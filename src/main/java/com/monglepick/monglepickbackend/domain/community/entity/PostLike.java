package com.monglepick.monglepickbackend.domain.community.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
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

/**
 * 게시글 좋아요 엔티티 — post_like 테이블 매핑.
 *
 * <p>사용자가 커뮤니티 게시글에 누른 좋아요를 기록한다.
 * 동일 사용자가 동일 게시글에 중복 좋아요를 누를 수 없다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code postId} — 게시글 ID</li>
 *   <li>{@code categoryId} — 카테고리 ID (nullable)</li>
 *   <li>{@code userId} — 좋아요를 누른 사용자 ID</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(user_id, post_id) — 동일 사용자가 동일 게시글에 중복 좋아요 불가.</p>
 *
 * <h3>타임스탬프</h3>
 * <p>created_at과 updated_at 모두 존재하므로 BaseTimeEntity를 상속한다.</p>
 */
@Entity
@Table(
        name = "post_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostLike extends BaseTimeEntity {

    /** 게시글 좋아요 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    /**
     * 게시글 ID (BIGINT, NOT NULL).
     * posts.id를 참조한다.
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 카테고리 ID (BIGINT, nullable).
     * category.category_id를 참조한다.
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 좋아요를 누른 사용자 ID (VARCHAR(50), NOT NULL).
     * users.user_id를 참조한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;
}
