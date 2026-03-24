package com.monglepick.monglepickbackend.domain.playlist.entity;

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
 * 플레이리스트 아이템 엔티티 — playlist_item 테이블 매핑.
 *
 * <p>{@link Playlist}에 포함된 개별 영화 항목을 저장한다.
 * 동일 플레이리스트에 동일 영화를 중복 추가할 수 없다.
 * sort_order로 영화의 정렬 순서를 지정할 수 있다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code playlistId} — 플레이리스트 ID (FK → playlist.playlist_id)</li>
 *   <li>{@code movieId} — 영화 ID</li>
 *   <li>{@code sortOrder} — 정렬 순서 (기본값: 0)</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(playlist_id, movie_id) — 동일 플레이리스트 내 영화 중복 불가.</p>
 *
 * <h3>타임스탬프</h3>
 * <p>added_at(created_at 역할)만 존재하며 updated_at은 없다.
 * BaseTimeEntity를 상속하지 않고 {@code @CreationTimestamp}를 직접 사용한다.</p>
 */
@Entity
@Table(
        name = "playlist_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "movie_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaylistItem {

    /** 플레이리스트 아이템 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    /**
     * 플레이리스트 ID (BIGINT, NOT NULL).
     * playlist.playlist_id를 참조한다.
     */
    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    /**
     * 영화 ID (VARCHAR(50), NOT NULL).
     * movies.movie_id를 참조한다.
     */
    @Column(name = "movie_id", length = 50, nullable = false)
    private String movieId;

    /**
     * 정렬 순서.
     * 기본값: 0.
     * 낮은 숫자일수록 앞쪽에 표시된다.
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 아이템 추가 시각.
     * INSERT 시 자동 설정되며 이후 변경되지 않는다.
     */
    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
