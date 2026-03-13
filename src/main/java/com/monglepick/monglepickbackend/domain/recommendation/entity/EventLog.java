package com.monglepick.monglepickbackend.domain.recommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 이벤트 로그 엔티티 — event_logs 테이블 매핑.
 *
 * <p>사용자의 추천 관련 이벤트(클릭, 스킵, 평가 등)를 기록한다.
 * 추천 알고리즘 개선과 사용자 행동 분석에 활용된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code userId} — 이벤트를 발생시킨 사용자 ID</li>
 *   <li>{@code movieId} — 관련 영화 ID (nullable, 영화와 무관한 이벤트 가능)</li>
 *   <li>{@code eventType} — 이벤트 유형 (click, skip, rate, search, view 등)</li>
 *   <li>{@code recommendScore} — 추천 당시 점수 (nullable)</li>
 *   <li>{@code metadata} — 추가 메타데이터 (JSON 형식)</li>
 * </ul>
 *
 * <h3>타임스탬프</h3>
 * <p>created_at만 존재하며 updated_at은 없다 (이벤트 로그 불변).
 * BaseTimeEntity를 상속하지 않고 {@code @CreationTimestamp}를 직접 사용한다.</p>
 */
@Entity
@Table(name = "event_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventLog {

    /** 이벤트 로그 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID (VARCHAR(50), NOT NULL).
     * 이벤트를 발생시킨 사용자를 식별한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /**
     * 영화 ID (VARCHAR(50), nullable).
     * 영화와 관련된 이벤트인 경우에만 설정된다.
     */
    @Column(name = "movie_id", length = 50)
    private String movieId;

    /**
     * 이벤트 유형 (VARCHAR(50), NOT NULL).
     * 예: "click"(클릭), "skip"(건너뛰기), "rate"(평가),
     *     "search"(검색), "view"(상세조회), "recommend"(추천받기)
     */
    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    /**
     * 추천 당시 점수 (nullable).
     * 추천 이벤트인 경우 해당 영화의 추천 점수를 기록한다.
     */
    @Column(name = "recommend_score")
    private Float recommendScore;

    /**
     * 추가 메타데이터 (JSON 형식).
     * 이벤트에 대한 부가 정보를 자유롭게 저장한다.
     * 예: {"source": "chat", "session_id": "uuid", "position": 1}
     */
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    /**
     * 레코드 생성 시각.
     * INSERT 시 자동 설정되며 이후 변경되지 않는다.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
