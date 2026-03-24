package com.monglepick.monglepickbackend.domain.community.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
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

/**
 * 게시글 댓글 엔티티 — post_comment 테이블 매핑.
 *
 * <p>커뮤니티 게시글에 달린 댓글을 저장한다.
 * 소프트 삭제(is_deleted)를 지원하여, 삭제된 댓글은 "삭제된 댓글입니다"로 표시한다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code postId} — 게시글 ID (FK → posts.id)</li>
 *   <li>{@code categoryId} — 카테고리 ID (nullable)</li>
 *   <li>{@code userId} — 작성자 ID</li>
 *   <li>{@code content} — 댓글 내용 (TEXT)</li>
 *   <li>{@code isDeleted} — 소프트 삭제 여부 (기본값: false)</li>
 * </ul>
 */
@Entity
@Table(name = "post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostComment extends BaseTimeEntity {

    /** 댓글 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

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
     * 작성자 사용자 ID (VARCHAR(50), NOT NULL).
     * users.user_id를 참조한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /** 댓글 내용 (TEXT 타입, NOT NULL) */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 소프트 삭제 여부.
     * 기본값: false.
     * true로 설정하면 댓글이 "삭제된 댓글입니다"로 표시된다.
     */
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
