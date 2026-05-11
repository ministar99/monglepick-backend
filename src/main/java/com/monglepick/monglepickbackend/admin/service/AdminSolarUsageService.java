package com.monglepick.monglepickbackend.admin.service;

import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageByAgent;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageByModel;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageDaily;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageLogRequest;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageStatsResponse;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageTotals;
import com.monglepick.monglepickbackend.domain.admin.entity.SolarApiUsageLog;
import com.monglepick.monglepickbackend.domain.admin.repository.SolarApiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upstage Solar API 사용량/비용 집계 서비스.
 *
 * <p>Agent 측 콜백이 적재한 {@code solar_api_usage_log} 행을 KPI/차트 단위로 집계해
 * 관리자 매출 탭의 "Solar API 사용/비용" 섹션에 공급한다. 또한 Agent 가 X-Service-Key
 * 인증으로 보낸 {@link SolarUsageLogRequest} 를 1행 INSERT 한다.</p>
 *
 * <h3>설계 메모</h3>
 * <ul>
 *   <li>클래스 레벨 {@code @Transactional(readOnly = true)} 로 집계 메서드 보호.
 *       INSERT 메서드만 메서드 레벨 {@code @Transactional} 로 readOnly 해제.
 *       (운영 패턴: AdminPaymentService.extendSubscription 회귀 학습 반영)</li>
 *   <li>일별 추이는 빈 날짜를 0 으로 채워 차트 X축 연속성 보장.</li>
 *   <li>비율(ratio)은 비용 기준 — 토큰 수가 적어도 비싼 모델이 부각되도록.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSolarUsageService {

    private final SolarApiUsageLogRepository solarApiUsageLogRepository;

    // ──────────────────────────────────────────────
    // INSERT — Agent fire-and-forget
    // ──────────────────────────────────────────────

    /**
     * Agent 가 보낸 단일 호출 로그를 적재한다.
     *
     * <p>Agent 측에서 비용 계산까지 마치고 보내므로 Backend 는 검증 후 단순 저장만 수행한다.
     * 실패 시 예외 전파(Agent 가 fire-and-forget 이라 무시) — 4xx/5xx 응답을 받아도
     * 사용자 경험에는 영향 없음.</p>
     *
     * @param req 적재 요청
     * @return 저장된 엔티티의 PK
     */
    @Transactional
    public Long appendUsageLog(SolarUsageLogRequest req) {
        SolarApiUsageLog log = SolarApiUsageLog.builder()
                .model(req.model())
                .agentName(req.agentName())
                .promptTokens(req.promptTokens())
                .completionTokens(req.completionTokens())
                .totalTokens(req.totalTokens())
                .estimatedCostUsd(req.estimatedCostUsd())
                .requestStartedAt(req.requestStartedAt())
                .durationMs(req.durationMs())
                .build();
        return solarApiUsageLogRepository.save(log).getSolarApiUsageId();
    }

    // ──────────────────────────────────────────────
    // 통계 응답 — 매출 탭 섹션
    // ──────────────────────────────────────────────

    /**
     * 매출 탭 "Solar API 사용/비용" 섹션 응답을 조립한다.
     *
     * @param period "7d" | "30d" | "90d" — 기본 30d
     * @return 통합 응답
     */
    public SolarUsageStatsResponse getUsageStats(String period) {
        int days = parseDays(period);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfYesterday = startOfToday.minusDays(1);
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfPeriod = startOfToday.minusDays(days - 1);

        SolarUsageTotals today = aggregateRange(startOfToday, now);
        SolarUsageTotals yesterday = aggregateRange(startOfYesterday, startOfToday);
        SolarUsageTotals thisMonth = aggregateRange(startOfThisMonth, now);
        SolarUsageTotals periodTotal = aggregateRange(startOfPeriod, now);
        SolarUsageTotals allTimeTotal = aggregateAll();

        List<SolarUsageDaily> dailyTrend = buildDailyTrend(startOfPeriod, now, days);
        List<SolarUsageByModel> byModel = buildByModel(startOfPeriod, now, periodTotal.costUsd());
        List<SolarUsageByAgent> byAgent = buildByAgent(startOfPeriod, now, periodTotal.costUsd());

        return new SolarUsageStatsResponse(
                period,
                today,
                yesterday,
                thisMonth,
                periodTotal,
                allTimeTotal,
                dailyTrend,
                byModel,
                byAgent
        );
    }

    // ──────────────────────────────────────────────
    // 내부 집계
    // ──────────────────────────────────────────────

    /** 단순 기간 합계. */
    private SolarUsageTotals aggregateRange(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = solarApiUsageLogRepository.aggregateRange(start, end);
        return rowToTotals(rows.isEmpty() ? null : rows.get(0));
    }

    /** 누적 합계. */
    private SolarUsageTotals aggregateAll() {
        List<Object[]> rows = solarApiUsageLogRepository.aggregateAll();
        return rowToTotals(rows.isEmpty() ? null : rows.get(0));
    }

    /**
     * Object[] → SolarUsageTotals 변환.
     *
     * <p>JPQL 의 SUM/COUNT 결과는 환경에 따라 BigDecimal/Long/Integer 가 섞여 나오므로
     * Number 로 받아 안전 변환한다.</p>
     */
    private SolarUsageTotals rowToTotals(Object[] row) {
        if (row == null) return SolarUsageTotals.zero();
        long prompt = toLong(row[0]);
        long completion = toLong(row[1]);
        long total = toLong(row[2]);
        BigDecimal cost = toBigDecimal(row[3]);
        long count = toLong(row[4]);
        return new SolarUsageTotals(prompt, completion, total, cost, count);
    }

    /**
     * 일별 추이 — 빈 날짜를 0 으로 채운다.
     *
     * @param start 기간 시작 (자정)
     * @param end   기간 끝 (현재)
     * @param days  채울 날짜 수
     */
    private List<SolarUsageDaily> buildDailyTrend(LocalDateTime start, LocalDateTime end, int days) {
        List<Object[]> rows = solarApiUsageLogRepository.aggregateDaily(start, end);

        Map<String, Object[]> byDate = new HashMap<>();
        for (Object[] row : rows) {
            String dateKey = formatDate(row[0]);
            byDate.put(dateKey, row);
        }

        List<SolarUsageDaily> out = new ArrayList<>(days);
        LocalDate startDate = start.toLocalDate();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            String key = d.toString();
            Object[] row = byDate.get(key);
            if (row == null) {
                out.add(new SolarUsageDaily(key, 0L, 0L, 0L, BigDecimal.ZERO, 0L));
            } else {
                out.add(new SolarUsageDaily(
                        key,
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toBigDecimal(row[4]),
                        toLong(row[5])
                ));
            }
        }
        return out;
    }

    /** 모델별 분포 — 비용 기준 비율 산정. */
    private List<SolarUsageByModel> buildByModel(LocalDateTime start, LocalDateTime end, BigDecimal totalCost) {
        List<Object[]> rows = solarApiUsageLogRepository.aggregateByModel(start, end);
        BigDecimal denom = (totalCost == null || totalCost.signum() == 0) ? BigDecimal.ONE : totalCost;
        List<SolarUsageByModel> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            BigDecimal cost = toBigDecimal(row[2]);
            double ratio = (totalCost == null || totalCost.signum() == 0)
                    ? 0.0
                    : cost.divide(denom, 4, RoundingMode.HALF_UP).doubleValue();
            out.add(new SolarUsageByModel(
                    (String) row[0],
                    toLong(row[1]),
                    cost,
                    toLong(row[3]),
                    ratio
            ));
        }
        return out;
    }

    /** 에이전트별 분포 — 비용 기준 비율 산정. */
    private List<SolarUsageByAgent> buildByAgent(LocalDateTime start, LocalDateTime end, BigDecimal totalCost) {
        List<Object[]> rows = solarApiUsageLogRepository.aggregateByAgent(start, end);
        BigDecimal denom = (totalCost == null || totalCost.signum() == 0) ? BigDecimal.ONE : totalCost;
        List<SolarUsageByAgent> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            BigDecimal cost = toBigDecimal(row[2]);
            double ratio = (totalCost == null || totalCost.signum() == 0)
                    ? 0.0
                    : cost.divide(denom, 4, RoundingMode.HALF_UP).doubleValue();
            out.add(new SolarUsageByAgent(
                    (String) row[0],
                    toLong(row[1]),
                    cost,
                    toLong(row[3]),
                    ratio
            ));
        }
        return out;
    }

    // ──────────────────────────────────────────────
    // 유틸
    // ──────────────────────────────────────────────

    private int parseDays(String period) {
        if (period == null) return 30;
        return switch (period) {
            case "7d" -> 7;
            case "90d" -> 90;
            default -> 30;
        };
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }

    /**
     * 일별 집계 결과의 첫 컬럼(JPQL FUNCTION('DATE', ...)) → "YYYY-MM-DD" 변환.
     *
     * <p>드라이버에 따라 java.sql.Date / java.time.LocalDate / String 으로 올 수 있어
     * 분기 처리.</p>
     */
    private String formatDate(Object v) {
        if (v == null) return "";
        if (v instanceof Date sqlDate) return sqlDate.toLocalDate().toString();
        if (v instanceof LocalDate ld) return ld.toString();
        if (v instanceof LocalDateTime ldt) return ldt.toLocalDate().toString();
        String s = v.toString();
        // "2026-05-11" 또는 "2026-05-11 00:00:00.0" 가능 — 앞 10자만.
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    /** 자정 시각 헬퍼 (테스트 용이성). */
    @SuppressWarnings("unused")
    private LocalDateTime atStartOfDay(LocalDate d) {
        return LocalDateTime.of(d, LocalTime.MIN);
    }
}
