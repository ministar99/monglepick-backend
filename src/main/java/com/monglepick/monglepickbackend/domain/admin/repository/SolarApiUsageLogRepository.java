package com.monglepick.monglepickbackend.domain.admin.repository;

import com.monglepick.monglepickbackend.domain.admin.entity.SolarApiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Solar API 사용 로그 리포지토리.
 *
 * <p>관리자페이지 매출 탭의 "Solar API 사용/비용" 섹션에서 사용하는 집계 쿼리들을 모아둔다.
 * 모든 집계 메서드는 native SQL 이 아닌 JPQL 로 작성되어 H2 단위 테스트 호환성을 유지한다.
 * 결과 행은 단순 Object[] 배열이며 서비스 레이어에서 DTO 로 매핑한다.</p>
 */
public interface SolarApiUsageLogRepository extends JpaRepository<SolarApiUsageLog, Long> {

    /**
     * 기간 내 전체 합계 — KPI 카드(이번달/누적 토큰·비용 등) 산정용.
     *
     * @return [promptTokens(Long), completionTokens(Long), totalTokens(Long),
     *          estimatedCostUsd(BigDecimal), callCount(Long)] 단일 row.
     *          호출 0건이어도 [0,0,0,0,0] 으로 반환되도록 COALESCE 처리.
     */
    @Query("""
            SELECT
                COALESCE(SUM(l.promptTokens), 0),
                COALESCE(SUM(l.completionTokens), 0),
                COALESCE(SUM(l.totalTokens), 0),
                COALESCE(SUM(l.estimatedCostUsd), 0),
                COUNT(l)
            FROM SolarApiUsageLog l
            WHERE l.requestStartedAt >= :startAt AND l.requestStartedAt < :endAt
            """)
    List<Object[]> aggregateRange(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    /**
     * 누적 전체 합계 (서비스 시작 이후) — KPI "누적" 카드용.
     *
     * @return [promptTokens, completionTokens, totalTokens, costUsd, callCount] 단일 row.
     */
    @Query("""
            SELECT
                COALESCE(SUM(l.promptTokens), 0),
                COALESCE(SUM(l.completionTokens), 0),
                COALESCE(SUM(l.totalTokens), 0),
                COALESCE(SUM(l.estimatedCostUsd), 0),
                COUNT(l)
            FROM SolarApiUsageLog l
            """)
    List<Object[]> aggregateAll();

    /**
     * 기간 내 일별 집계 — 차트용.
     *
     * <p>JPQL 의 {@code FUNCTION('DATE', ...)} 으로 날짜만 잘라 GROUP BY 한다.
     * 결과는 날짜 오름차순. 빈 날짜는 누락되며 서비스 레이어에서 0 으로 채운다.</p>
     *
     * @return List of [date(java.sql.Date), promptTokens, completionTokens,
     *                  totalTokens, estimatedCostUsd, callCount]
     */
    @Query("""
            SELECT
                FUNCTION('DATE', l.requestStartedAt),
                COALESCE(SUM(l.promptTokens), 0),
                COALESCE(SUM(l.completionTokens), 0),
                COALESCE(SUM(l.totalTokens), 0),
                COALESCE(SUM(l.estimatedCostUsd), 0),
                COUNT(l)
            FROM SolarApiUsageLog l
            WHERE l.requestStartedAt >= :startAt AND l.requestStartedAt < :endAt
            GROUP BY FUNCTION('DATE', l.requestStartedAt)
            ORDER BY FUNCTION('DATE', l.requestStartedAt) ASC
            """)
    List<Object[]> aggregateDaily(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    /**
     * 기간 내 모델별 집계 — Pie/Bar 차트용.
     *
     * @return List of [model(String), totalTokens, estimatedCostUsd, callCount]
     */
    @Query("""
            SELECT
                l.model,
                COALESCE(SUM(l.totalTokens), 0),
                COALESCE(SUM(l.estimatedCostUsd), 0),
                COUNT(l)
            FROM SolarApiUsageLog l
            WHERE l.requestStartedAt >= :startAt AND l.requestStartedAt < :endAt
            GROUP BY l.model
            ORDER BY SUM(l.estimatedCostUsd) DESC
            """)
    List<Object[]> aggregateByModel(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    /**
     * 기간 내 에이전트별 집계 — Pie/Bar 차트용.
     *
     * @return List of [agentName(String), totalTokens, estimatedCostUsd, callCount]
     */
    @Query("""
            SELECT
                l.agentName,
                COALESCE(SUM(l.totalTokens), 0),
                COALESCE(SUM(l.estimatedCostUsd), 0),
                COUNT(l)
            FROM SolarApiUsageLog l
            WHERE l.requestStartedAt >= :startAt AND l.requestStartedAt < :endAt
            GROUP BY l.agentName
            ORDER BY SUM(l.estimatedCostUsd) DESC
            """)
    List<Object[]> aggregateByAgent(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
