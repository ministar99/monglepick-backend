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
 * 커뮤니티 카테고리 엔티티 — category 테이블 매핑.
 *
 * <p>커뮤니티 게시판의 상위 카테고리를 정의한다.
 * 하위 카테고리는 {@link CategoryChild}에서 관리된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code categoryId} — 카테고리 고유 ID (PK)</li>
 *   <li>{@code upCategory} — 상위 카테고리명 (UNIQUE)</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(up_category) — 상위 카테고리명은 중복될 수 없다.</p>
 */
@Entity
@Table(
        name = "category",
        uniqueConstraints = @UniqueConstraint(columnNames = "up_category")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends BaseTimeEntity {

    /** 카테고리 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 상위 카테고리명 (VARCHAR(100), NOT NULL, UNIQUE).
     * 예: "자유게시판", "영화토론", "리뷰", "질문"
     */
    @Column(name = "up_category", length = 100, nullable = false)
    private String upCategory;
}
