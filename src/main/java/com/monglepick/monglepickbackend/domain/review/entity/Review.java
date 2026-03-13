package com.monglepick.monglepickbackend.domain.review.entity;

import com.monglepick.monglepickbackend.domain.movie.entity.Movie;
import com.monglepick.monglepickbackend.domain.user.entity.User;
import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
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

/**
 * 영화 리뷰 엔티티 — reviews 테이블 매핑.
 *
 * <p>사용자가 특정 영화에 대해 작성하는 리뷰를 저장한다.
 * 한 사용자는 하나의 영화에 대해 하나의 리뷰만 작성할 수 있다
 * (user_id, movie_id UNIQUE 제약).</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 리뷰 작성자 (FK → users.user_id)</li>
 *   <li>{@code movie} — 리뷰 대상 영화 (FK → movies.movie_id)</li>
 *   <li>{@code rating} — 평점 (0.5~5.0, 필수)</li>
 *   <li>{@code content} — 리뷰 본문 (선택, TEXT)</li>
 *   <li>{@code spoiler} — 스포일러 포함 여부 (기본값: false)</li>
 *   <li>{@code likeCount} — 좋아요 수 (기본값: 0)</li>
 * </ul>
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_movie", columnNames = {"user_id", "movie_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    /** 리뷰 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리뷰 작성자.
     * reviews.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 리뷰 대상 영화.
     * reviews.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * 평점 (0.5~5.0, 필수).
     * 0.5 단위로 평가할 수 있다 (0.5, 1.0, 1.5, ..., 5.0).
     */
    @Column(name = "rating", nullable = false)
    private Float rating;

    /** 리뷰 본문 내용 (선택, TEXT 타입) */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 스포일러 포함 여부.
     * true이면 리뷰 내용이 스포일러를 포함하므로 클라이언트에서 블러 처리 등을 한다.
     * 기본값: false
     */
    @Column(name = "spoiler")
    @Builder.Default
    private Boolean spoiler = false;

    /** 좋아요 수 (기본값: 0) */
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;
}
