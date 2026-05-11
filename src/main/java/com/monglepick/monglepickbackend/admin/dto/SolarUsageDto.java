package com.monglepick.monglepickbackend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Upstage Solar API 사용량/비용 통계 DTO 모음.
 *
 * <p>관리자 매출 탭의 "Solar API 사용/비용" 섹션 응답과 Agent → Backend
 * usage 적재 요청을 담당한다.</p>
 *
 * <h3>포함된 DTO 목록</h3>
 * <ul>
 *   <li>{@link SolarUsageLogRequest} — Agent 가 X-Service-Key 인증으로 1건 적재</li>
 *   <li>{@link SolarUsageStatsResponse} — 매출 탭 섹션 전체 응답</li>
 *   <li>{@link SolarUsageTotals} — 기간/누적 KPI 합계</li>
 *   <li>{@link SolarUsageDaily} — 일별 추이 차트 단일 데이터포인트</li>
 *   <li>{@link SolarUsageByModel} — 모델별 분포 (Pie/Bar)</li>
 *   <li>{@link SolarUsageByAgent} — 에이전트별 분포 (Pie/Bar)</li>
 * </ul>
 */
public class SolarUsageDto {

    private SolarUsageDto() {
        /* utility-style holder */
    }

    // ──────────────────────────────────────────────
    // Agent → Backend 적재 요청
    // ──────────────────────────────────────────────

    /**
     * Solar API 호출 1건 적재 요청.
     *
     * <p>AI Agent 측 LangChain 콜백(`SolarUsageCallback`) 이 X-Service-Key 인증으로
     * fire-and-forget 으로 보낸다. 비용 계산(estimatedCostUsd)은 Agent 측에서
     * {@code monglepick.llm.solar_pricing} 의 모델 단가를 곱해 미리 산정한다.</p>
     *
     * @param model              Solar 모델명 (예: "solar-pro", "solar-mini")
     * @param agentName          호출 주체 (예: "chat", "match", "admin_assistant", "unknown")
     * @param promptTokens       입력 토큰 수
     * @param completionTokens   출력 토큰 수 (임베딩이면 0)
     * @param totalTokens        총 토큰 수 (일반적으로 prompt+completion)
     * @param estimatedCostUsd   비용 추정 USD (소수 6자리)
     * @param requestStartedAt   호출 시작 시각 (ISO 8601)
     * @param durationMs         호출 소요 시간 ms (nullable, 측정 실패 시 null)
     */
    @Schema(description = "Solar API 호출 1건 적재 요청")
    public record SolarUsageLogRequest(
            @NotBlank
            @Size(max = 50)
            @Schema(example = "solar-pro")
            String model,

            @NotBlank
            @Size(max = 100)
            @Schema(example = "chat")
            String agentName,

            @NotNull
            @Min(0)
            Integer promptTokens,

            @NotNull
            @Min(0)
            Integer completionTokens,

            @NotNull
            @Min(0)
            Integer totalTokens,

            @NotNull
            BigDecimal estimatedCostUsd,

            @NotNull
            LocalDateTime requestStartedAt,

            Integer durationMs
    ) {}

    // ──────────────────────────────────────────────
    // 매출 탭 섹션 응답
    // ──────────────────────────────────────────────

    /**
     * Solar API 사용량/비용 섹션 통합 응답.
     *
     * @param period         질의된 기간 (예: "7d", "30d", "90d")
     * @param today          오늘(자정~지금) 합계
     * @param yesterday      어제 합계
     * @param thisMonth      이번 달 (월초~지금) 합계
     * @param periodTotal    선택 기간 합계
     * @param allTimeTotal   누적 (서비스 시작 이후)
     * @param dailyTrend     일별 추이 (선택 기간, 빈 날짜 0 으로 채움)
     * @param byModel        모델별 분포 (선택 기간)
     * @param byAgent        에이전트별 분포 (선택 기간)
     */
    @Schema(description = "Solar API 사용량/비용 섹션 통합 응답")
    public record SolarUsageStatsResponse(
            String period,
            SolarUsageTotals today,
            SolarUsageTotals yesterday,
            SolarUsageTotals thisMonth,
            SolarUsageTotals periodTotal,
            SolarUsageTotals allTimeTotal,
            List<SolarUsageDaily> dailyTrend,
            List<SolarUsageByModel> byModel,
            List<SolarUsageByAgent> byAgent
    ) {}

    /**
     * 합계 KPI — 토큰/비용/호출수.
     *
     * @param promptTokens     누계 입력 토큰
     * @param completionTokens 누계 출력 토큰
     * @param totalTokens      누계 총 토큰
     * @param costUsd          누계 비용 USD
     * @param callCount        호출 건수
     */
    @Schema(description = "Solar API 사용량 합계 KPI")
    public record SolarUsageTotals(
            long promptTokens,
            long completionTokens,
            long totalTokens,
            BigDecimal costUsd,
            long callCount
    ) {
        /** 0 합계 — 데이터 없는 기간 응답에 사용. */
        public static SolarUsageTotals zero() {
            return new SolarUsageTotals(0L, 0L, 0L, BigDecimal.ZERO, 0L);
        }
    }

    /**
     * 일별 추이 데이터포인트.
     *
     * @param date         "YYYY-MM-DD"
     * @param promptTokens 입력 토큰 합
     * @param completionTokens 출력 토큰 합
     * @param totalTokens  총 토큰 합
     * @param costUsd      비용 USD 합
     * @param callCount    호출 건수
     */
    @Schema(description = "Solar API 일별 사용량")
    public record SolarUsageDaily(
            String date,
            long promptTokens,
            long completionTokens,
            long totalTokens,
            BigDecimal costUsd,
            long callCount
    ) {}

    /**
     * 모델별 분포 (Pie/Bar).
     *
     * @param model       모델명
     * @param totalTokens 총 토큰
     * @param costUsd     비용 USD
     * @param callCount   호출 건수
     * @param ratio       비용 기준 비율 (0.0~1.0)
     */
    @Schema(description = "Solar API 모델별 분포")
    public record SolarUsageByModel(
            String model,
            long totalTokens,
            BigDecimal costUsd,
            long callCount,
            double ratio
    ) {}

    /**
     * 에이전트별 분포 (Pie/Bar).
     *
     * @param agentName   에이전트/체인 이름
     * @param totalTokens 총 토큰
     * @param costUsd     비용 USD
     * @param callCount   호출 건수
     * @param ratio       비용 기준 비율 (0.0~1.0)
     */
    @Schema(description = "Solar API 에이전트별 분포")
    public record SolarUsageByAgent(
            String agentName,
            long totalTokens,
            BigDecimal costUsd,
            long callCount,
            double ratio
    ) {}
}
