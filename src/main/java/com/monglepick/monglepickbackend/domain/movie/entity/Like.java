package com.monglepick.monglepickbackend.domain.movie.entity;

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
 * 영화 좋아요 엔티티 — likes 테이블 매핑.
 *
 * <p>사용자가 특정 영화에 좋아요를 누른 기록을 저장한다.
 * 소프트 삭제(deleted_at)를 지원하여, 좋아요 취소 시 레코드를 삭제하지 않고
 * deleted_at에 삭제 시각을 기록한다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code userId} — 사용자 ID</li>
 *   <li>{@code movieId} — 영화 ID</li>
 *   <li>{@code deletedAt} — 소프트 삭제 시각 (null이면 활성 좋아요)</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(user_id, movie_id) — 동일 사용자가 동일 영화에 중복 좋아요 불가.</p>
 *
 * <h3>타임스탬프</h3>
 * <p>created_at만 존재 (좋아요는 수정 개념이 없으므로 updated_at 불필요).
 * BaseTimeEntity를 상속하지 않고 {@code @CreationTimestamp}를 직접 사용한다.</p>
 */
@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like {

    /** 좋아요 레코드 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID (VARCHAR(50), NOT NULL).
     * users.user_id를 참조한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /**
     * 영화 ID (VARCHAR(50), NOT NULL).
     * movies.movie_id를 참조한다.
     */
    @Column(name = "movie_id", length = 50, nullable = false)
    private String movieId;

    /**
     * 레코드 생성 시각.
     * INSERT 시 자동 설정되며 이후 변경되지 않는다.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 소프트 삭제 시각 (nullable).
     * null이면 활성 좋아요, 값이 있으면 좋아요가 취소된 상태.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
