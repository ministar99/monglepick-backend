package com.monglepick.monglepickbackend.domain.roadmap.entity;

import com.monglepick.monglepickbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 업적/뱃지 엔티티 — user_achievements 테이블 매핑.
 *
 * <p>도장깨기 코스 완주, 퀴즈 만점, 리뷰 작성 등 다양한 활동에 대한
 * 업적/뱃지를 기록한다. 같은 유형+키의 업적은 중복 달성이 불가하다
 * (user_id, achievement_type, achievement_key UNIQUE 제약).</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 업적 달성 사용자 (FK → users.user_id)</li>
 *   <li>{@code achievementType} — 업적 유형 (예: "course_complete", "quiz_perfect", "review_count")</li>
 *   <li>{@code achievementKey} — 업적 식별 키 (예: 코스 ID, 수치 등)</li>
 *   <li>{@code achievedAt} — 업적 달성 시각</li>
 *   <li>{@code metadata} — 추가 메타데이터 (JSON)</li>
 * </ul>
 *
 * <h3>업적 유형 예시</h3>
 * <ul>
 *   <li>{@code course_complete} — 코스 완주 (key: 코스 ID)</li>
 *   <li>{@code quiz_perfect} — 퀴즈 만점 (key: 코스 ID)</li>
 *   <li>{@code review_count_10} — 리뷰 10개 작성</li>
 *   <li>{@code genre_explorer} — 장르 탐험가 (5개 이상 장르 시청)</li>
 * </ul>
 */
@Entity
@Table(name = "user_achievements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_achievement", columnNames = {"user_id", "achievement_type", "achievement_key"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAchievement {

    /** 업적 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업적 달성 사용자.
     * user_achievements.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 업적 유형 (최대 50자).
     * 예: "course_complete", "quiz_perfect", "review_count", "genre_explorer"
     */
    @Column(name = "achievement_type", length = 50, nullable = false)
    private String achievementType;

    /**
     * 업적 식별 키 (최대 100자).
     * 같은 유형 내에서 구체적인 업적을 구분하는 키.
     * 예: 코스 ID("nolan-filmography"), 수치("10"), 장르("horror")
     */
    @Column(name = "achievement_key", length = 100, nullable = false)
    private String achievementKey;

    /** 업적 달성 시각 */
    @CreationTimestamp
    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    /**
     * 추가 메타데이터 (JSON 객체).
     * 업적과 관련된 부가 정보를 자유롭게 저장한다.
     * 예: {"score": 100, "time_taken": "2h 30m", "movies_watched": 12}
     */
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
