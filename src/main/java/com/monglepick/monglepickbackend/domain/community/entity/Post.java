package com.monglepick.monglepickbackend.domain.community.entity;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커뮤니티 게시글 엔티티 — posts 테이블 매핑.
 *
 * <p>사용자가 작성하는 커뮤니티 게시글을 저장한다.
 * 특정 영화에 대한 게시글일 경우 movie_id FK로 연결할 수 있다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 작성자 (FK → users.user_id)</li>
 *   <li>{@code title} — 게시글 제목 (필수, 최대 300자)</li>
 *   <li>{@code content} — 게시글 본문 (필수, TEXT)</li>
 *   <li>{@code categoryId} — 카테고리 ID (FK → category.category_id)</li>
 *   <li>{@code status} — 게시글 상태 (기본값: "active")</li>
 *   <li>{@code movie} — 관련 영화 (선택, FK → movies.movie_id)</li>
 *   <li>{@code likeCount} — 좋아요 수 (기본값: 0)</li>
 *   <li>{@code commentCount} — 댓글 수 (기본값: 0)</li>
 *   <li>{@code viewCount} — 조회수 (기본값: 0)</li>
 * </ul>
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseTimeEntity {

    /** 게시글 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게시글 작성자.
     * posts.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 게시글 제목 (필수, 최대 300자) */
    @Column(name = "title", length = 300, nullable = false)
    private String title;

    /** 게시글 본문 내용 (필수, TEXT 타입) */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 카테고리 ID.
     * posts.category_id → category.category_id FK.
     * 게시글이 속한 카테고리를 참조한다.
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 게시글 상태 (최대 20자).
     * 기본값: "active".
     * 예: "active"(활성), "hidden"(숨김), "deleted"(삭제)
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "active";

    /**
     * 관련 영화 (선택).
     * 특정 영화에 대한 게시글인 경우에만 연결된다.
     * posts.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    /** 좋아요 수 (기본값: 0) */
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /** 댓글 수 (기본값: 0) */
    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    /** 조회수 (기본값: 0) */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
}
