package com.monglepick.monglepickbackend.domain.search.entity;

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
 * 검색 이력 엔티티 — search_history 테이블 매핑.
 *
 * <p>사용자의 검색 키워드 이력을 저장한다.
 * 최근 검색어 표시 및 개인화 검색 추천에 활용된다.
 * 한 사용자가 같은 키워드를 다시 검색하면 기존 레코드가 업데이트된다
 * (user_id, keyword UNIQUE 제약).</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 검색한 사용자 (FK → users.user_id)</li>
 *   <li>{@code keyword} — 검색 키워드 (최대 200자, 필수)</li>
 *   <li>{@code searchedAt} — 검색 시각</li>
 * </ul>
 */
@Entity
@Table(name = "search_history", uniqueConstraints = {
        @UniqueConstraint(name = "uk_search_history_user_keyword", columnNames = {"user_id", "keyword"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SearchHistory {

    /** 검색 이력 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 검색한 사용자.
     * search_history.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 검색 키워드 (최대 200자, 필수) */
    @Column(name = "keyword", length = 200, nullable = false)
    private String keyword;

    /** 검색 시각 (동일 키워드 재검색 시 이 값이 갱신된다) */
    @CreationTimestamp
    @Column(name = "searched_at")
    private LocalDateTime searchedAt;
}
