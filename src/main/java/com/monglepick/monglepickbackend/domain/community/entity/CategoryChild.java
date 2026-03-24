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
 * 하위 카테고리 엔티티 — category_child 테이블 매핑.
 *
 * <p>상위 카테고리({@link Category}) 하위의 세부 카테고리를 정의한다.
 * 동일 상위 카테고리 내에서 하위 카테고리명은 중복될 수 없다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code categoryId} — 상위 카테고리 ID (FK → category.category_id)</li>
 *   <li>{@code categoryChild} — 하위 카테고리명</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(category_id, category_child) — 동일 상위 카테고리 내 하위명 중복 불가.</p>
 */
@Entity
@Table(
        name = "category_child",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "category_child"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryChild extends BaseTimeEntity {

    /** 하위 카테고리 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "down_category_id")
    private Long downCategoryId;

    /**
     * 상위 카테고리 ID (BIGINT, NOT NULL).
     * category.category_id를 참조한다.
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 하위 카테고리명 (VARCHAR(100), NOT NULL).
     * 예: "일반", "스포일러", "추천", "비추"
     */
    @Column(name = "category_child", length = 100, nullable = false)
    private String categoryChild;
}
