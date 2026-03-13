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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 위시리스트(찜 목록) 엔티티 — user_wishlist 테이블 매핑.
 *
 * <p>사용자가 "나중에 볼 영화"로 저장한 영화 목록이다.
 * 한 사용자가 같은 영화를 중복 찜할 수 없도록 (user_id, movie_id) UNIQUE 제약이 있다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 찜한 사용자 (FK → users.user_id)</li>
 *   <li>{@code movie} — 찜한 영화 (FK → movies.movie_id)</li>
 *   <li>{@code createdAt} — 찜한 시각</li>
 * </ul>
 */
@Entity
@Table(name = "user_wishlist", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_user_movie", columnNames = {"user_id", "movie_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserWishlist {

    /** 위시리스트 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 찜한 사용자.
     * user_wishlist.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 찜한 영화.
     * user_wishlist.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** 찜한 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
