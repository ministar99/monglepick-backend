package com.monglepick.monglepickbackend.domain.admin.repository;

import com.monglepick.monglepickbackend.domain.admin.entity.SolarApiUsageLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SolarApiUsageLogRepository 의 집계 JPQL 들이 H2 + Hibernate 7 환경에서 정상 동작하는지
 * 검증하는 통합 테스트.
 *
 * <p>회귀 안전망:
 * <ul>
 *   <li>{@code aggregateRange} / {@code aggregateAll} 가 항상 1행 반환 (COALESCE 처리).</li>
 *   <li>{@code aggregateDaily} 가 날짜별 GROUP BY 로 다중 행을 반환.</li>
 *   <li>{@code aggregateByModel} / {@code aggregateByAgent} 가 비용 내림차순 정렬.</li>
 * </ul></p>
 *
 * <p>데이터 0 행 케이스도 함께 검증 — 운영 신규 배포 직후 첫 화면 진입 시 깨지지 않게.</p>
 */
@SpringBootTest
@Transactional
@DisplayName("SolarApiUsageLogRepository — 집계 JPQL 통합 테스트 (2026-05-11)")
class SolarApiUsageLogRepositoryTest {

    @Autowired
    private SolarApiUsageLogRepository repository;

    @Test
    @DisplayName("데이터 0건 — aggregateRange 는 [0,0,0,0,0] 1행, aggregateDaily 는 빈 리스트")
    void emptyAggregation() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        List<Object[]> range = repository.aggregateRange(start, end);
        assertThat(range).hasSize(1);
        Object[] row = range.get(0);
        assertThat(((Number) row[0]).longValue()).isEqualTo(0L);
        assertThat(((Number) row[1]).longValue()).isEqualTo(0L);
        assertThat(((Number) row[2]).longValue()).isEqualTo(0L);
        // BigDecimal SUM — null 일 수 있으므로 number 캐스팅 후 비교
        assertThat(((Number) row[3]).doubleValue()).isEqualTo(0.0);
        assertThat(((Number) row[4]).longValue()).isEqualTo(0L);

        List<Object[]> daily = repository.aggregateDaily(start, end);
        assertThat(daily).isEmpty();

        List<Object[]> all = repository.aggregateAll();
        assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("3건 적재 — range/daily/byModel/byAgent 합계와 그룹핑이 정확")
    void aggregateMultipleEntries() {
        LocalDateTime now = LocalDateTime.now();
        // 같은 날짜에 2건, 다른 날짜에 1건 — 그룹핑 검증용
        repository.save(SolarApiUsageLog.builder()
                .model("solar-pro")
                .agentName("chat")
                .promptTokens(100).completionTokens(50).totalTokens(150)
                .estimatedCostUsd(new BigDecimal("0.000045"))
                .requestStartedAt(now.minusDays(2))
                .durationMs(1200)
                .build());
        repository.save(SolarApiUsageLog.builder()
                .model("solar-pro")
                .agentName("match")
                .promptTokens(200).completionTokens(80).totalTokens(280)
                .estimatedCostUsd(new BigDecimal("0.000078"))
                .requestStartedAt(now.minusDays(2))
                .durationMs(900)
                .build());
        repository.save(SolarApiUsageLog.builder()
                .model("solar-mini")
                .agentName("chat")
                .promptTokens(50).completionTokens(20).totalTokens(70)
                .estimatedCostUsd(new BigDecimal("0.0000105"))
                .requestStartedAt(now.minusDays(1))
                .durationMs(450)
                .build());

        // range — 모두 포함
        LocalDateTime start = now.minusDays(7);
        LocalDateTime end = now.plusMinutes(1);
        Object[] rangeRow = repository.aggregateRange(start, end).get(0);
        assertThat(((Number) rangeRow[0]).longValue()).isEqualTo(350L);  // prompt total
        assertThat(((Number) rangeRow[1]).longValue()).isEqualTo(150L);  // completion total
        assertThat(((Number) rangeRow[2]).longValue()).isEqualTo(500L);  // total tokens
        assertThat(((Number) rangeRow[4]).longValue()).isEqualTo(3L);    // call count

        // daily — 2개 날짜로 그룹핑
        List<Object[]> daily = repository.aggregateDaily(start, end);
        assertThat(daily).hasSize(2);

        // byModel — solar-pro 비용이 더 크므로 먼저 와야 함 (DESC 정렬)
        List<Object[]> byModel = repository.aggregateByModel(start, end);
        assertThat(byModel).hasSize(2);
        assertThat((String) byModel.get(0)[0]).isEqualTo("solar-pro");
        assertThat(((Number) byModel.get(0)[1]).longValue()).isEqualTo(430L); // 150+280 tokens
        assertThat((String) byModel.get(1)[0]).isEqualTo("solar-mini");

        // byAgent — chat 비용 합 vs match 비용 — chat 이 0.000045 + 0.0000105 = 0.0000555,
        // match 가 0.000078 → match 가 더 큼.
        List<Object[]> byAgent = repository.aggregateByAgent(start, end);
        assertThat(byAgent).hasSize(2);
        assertThat((String) byAgent.get(0)[0]).isEqualTo("match");
        assertThat((String) byAgent.get(1)[0]).isEqualTo("chat");
    }
}
