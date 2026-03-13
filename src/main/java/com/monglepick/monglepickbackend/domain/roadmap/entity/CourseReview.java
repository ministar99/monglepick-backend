package com.monglepick.monglepickbackend.domain.roadmap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * 도장깨기 코스 리뷰 엔티티 — course_review 테이블 매핑.
 *
 * <p>도장깨기(로드맵) 코스에서 영화를 시청한 후 작성하는 리뷰를 저장한다.
 * 동일 코스의 동일 영화에 대해 사용자당 하나의 리뷰만 작성 가능하다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code courseId} — 코스 ID (VARCHAR(50))</li>
 *   <li>{@code movieId} — 영화 ID (VARCHAR(50))</li>
 *   <li>{@code userId} — 작성자 ID (VARCHAR(50))</li>
 *   <li>{@code reviewText} — 리뷰 본문 (TEXT, nullable)</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(course_id, movie_id, user_id) — 동일 코스+영화+사용자 조합에 중복 리뷰 불가.</p>
 *
 * <h3>타임스탬프</h3>
 * <p>created_at만 존재하며 updated_at은 없다.
 * BaseTimeEntity를 상속하지 않고 {@code @CreationTimestamp}를 직접 사용한다.</p>
 */
@Entity
@Table(
        name = "course_review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "movie_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseReview {

    /** 코스 리뷰 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_review_id")
    private Long courseReviewId;

    /**
     * 코스 ID (VARCHAR(50), NOT NULL).
     * 도장깨기 코스를 식별한다.
     */
    @Column(name = "course_id", length = 50, nullable = false)
    private String courseId;

    /**
     * 영화 ID (VARCHAR(50), NOT NULL).
     * movies.movie_id를 참조한다.
     */
    @Column(name = "movie_id", length = 50, nullable = false)
    private String movieId;

    /**
     * 작성자 사용자 ID (VARCHAR(50), NOT NULL).
     * users.user_id를 참조한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /**
     * 리뷰 본문 (TEXT 타입, nullable).
     * 텍스트 리뷰가 없이 시청 완료만 기록하는 경우 null.
     */
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    /**
     * 레코드 생성 시각.
     * INSERT 시 자동 설정되며 이후 변경되지 않는다.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
