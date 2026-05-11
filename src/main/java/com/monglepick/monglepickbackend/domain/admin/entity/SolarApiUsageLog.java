package com.monglepick.monglepickbackend.domain.admin.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Upstage Solar API 호출 사용량 로그 엔티티 — solar_api_usage_log 테이블 매핑.
 *
 * <p>모든 Solar API LLM 호출(채팅 completion · 임베딩 등)의 토큰 사용량과
 * 비용 추정치를 1행씩 적재한다. AI Agent 측 LangChain 콜백
 * (`SolarUsageCallback`) 이 X-Service-Key 인증으로
 * {@code POST /api/v1/admin/stats/solar-usage/log} 에 fire-and-forget 으로 INSERT 한다.</p>
 *
 * <h3>운영상 의도</h3>
 * <ul>
 *   <li>관리자페이지 매출 탭 "Solar API 사용/비용" 섹션의 진실 원본.</li>
 *   <li>일별 토큰/비용 추이, 모델·에이전트별 비용 분포 집계 입력원.</li>
 *   <li>이상 비용 탐지(특정 일자 비정상 폭증) 의 1차 데이터.</li>
 * </ul>
 *
 * <h3>비용 산정</h3>
 * <p>{@code estimatedCostUsd} 는 Agent 측에서 {@code monglepick.llm.solar_pricing}
 * 모델 단가를 곱해 산정한 추정치이며, Upstage 청구서 정합 검증의 책임은 운영자에게 있다.
 * 단가 변경 시 Agent 상수만 수정하면 신규 호출부터 반영되지만, 과거 행은 갱신되지 않는다.</p>
 *
 * <h3>인덱스 설계</h3>
 * <ul>
 *   <li>{@code idx_solar_usage_request_started_at} — 일별/기간 집계 (기본 정렬 키)</li>
 *   <li>{@code idx_solar_usage_model} — 모델별 분포 집계</li>
 *   <li>{@code idx_solar_usage_agent_name} — 에이전트별 분포 집계</li>
 * </ul>
 */
@Entity
@Table(
        name = "solar_api_usage_log",
        indexes = {
                @Index(name = "idx_solar_usage_request_started_at", columnList = "request_started_at"),
                @Index(name = "idx_solar_usage_model", columnList = "model"),
                @Index(name = "idx_solar_usage_agent_name", columnList = "agent_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SolarApiUsageLog extends BaseTimeEntity {

    /**
     * 호출 로그 고유 ID (BIGINT AUTO_INCREMENT PK).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solar_api_usage_id")
    private Long solarApiUsageId;

    /**
     * 호출된 Solar 모델명 (VARCHAR(50), NOT NULL).
     * 예: "solar-pro", "solar-pro2", "solar-pro3", "solar-mini".
     * 단가 적용 시 이 값을 키로 {@code SolarPricingTable} 을 조회한다.
     */
    @Column(name = "model", length = 50, nullable = false)
    private String model;

    /**
     * 호출 주체 에이전트/체인 이름 (VARCHAR(100), NOT NULL).
     * 예: "chat", "match", "admin_assistant", "support_assistant", "data_pipeline", "unknown".
     * Agent 측 contextvar 에서 채워지며 비어있으면 "unknown" 으로 저장된다.
     */
    @Column(name = "agent_name", length = 100, nullable = false)
    private String agentName;

    /**
     * 입력 토큰 수 (NOT NULL).
     * Solar API 응답의 {@code usage.prompt_tokens}.
     */
    @Column(name = "prompt_tokens", nullable = false)
    private Integer promptTokens;

    /**
     * 출력 토큰 수 (NOT NULL).
     * Solar API 응답의 {@code usage.completion_tokens}.
     * 임베딩 호출의 경우 0 으로 적재된다.
     */
    @Column(name = "completion_tokens", nullable = false)
    private Integer completionTokens;

    /**
     * 총 토큰 수 (NOT NULL).
     * Solar API 응답의 {@code usage.total_tokens} — 일반적으로
     * promptTokens + completionTokens 와 같다.
     */
    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    /**
     * 추정 비용 USD (DECIMAL(12,6), NOT NULL).
     * Agent 측에서 {@code SolarPricingTable} 의 단가($/1M tokens) 를 적용해 계산.
     * 소수 6자리(마이크로 USD) 까지 보존하여 작은 호출도 누적 가능.
     */
    @Column(name = "estimated_cost_usd", precision = 12, scale = 6, nullable = false)
    private BigDecimal estimatedCostUsd;

    /**
     * Solar API 호출 시작 시각 (NOT NULL).
     * 일별 집계의 기준 타임스탬프 — created_at 보다 정확하다(콜백 비동기 지연 보정).
     */
    @Column(name = "request_started_at", nullable = false)
    private LocalDateTime requestStartedAt;

    /**
     * Solar API 호출 소요 시간 (밀리초, nullable).
     * 콜백에서 측정 실패 시 NULL 로 적재된다.
     */
    @Column(name = "duration_ms")
    private Integer durationMs;

    /* created_at, updated_at → BaseTimeEntity 상속 */
}
