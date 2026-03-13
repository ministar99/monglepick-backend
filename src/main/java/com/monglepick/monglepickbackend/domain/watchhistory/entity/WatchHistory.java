package com.monglepick.monglepickbackend.domain.watchhistory.entity;

import com.monglepick.monglepickbackend.domain.movie.entity.Movie;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 시청 이력 엔티티 — watch_history 테이블 매핑.
 *
 * <p>사용자가 시청한 영화와 평점을 기록한다.
 * CF(Collaborative Filtering) 추천의 핵심 데이터 소스이며,
 * 한 사용자가 같은 영화를 여러 번 시청(재시청)할 수 있으므로
 * user_id + movie_id에 UNIQUE 제약이 없다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 시청한 사용자 (FK → users.user_id)</li>
 *   <li>{@code movie} — 시청한 영화 (FK → movies.movie_id)</li>
 *   <li>{@code rating} — 사용자 평점 (0.5~5.0, 선택)</li>
 *   <li>{@code watchedAt} — 시청 시각</li>
 * </ul>
 *
 * <h3>주의사항</h3>
 * <p>이 테이블은 약 2,600만 행으로, 대량 데이터를 보유한다.
 * 인덱스: idx_wh_user(user_id), idx_wh_movie(movie_id), idx_wh_user_movie(user_id, movie_id)</p>
 */
@Entity
@Table(name = "watch_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WatchHistory {

    /** 시청 이력 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시청한 사용자.
     * watch_history.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 시청한 영화.
     * watch_history.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * 사용자 평점 (0.5~5.0).
     * 시청만 하고 평점을 매기지 않을 수 있으므로 nullable이다.
     */
    @Column(name = "rating")
    private Float rating;

    /** 시청 시각 (기본값: 레코드 생성 시각) */
    @CreationTimestamp
    @Column(name = "watched_at")
    private LocalDateTime watchedAt;

    /** 레코드 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
