package com.monglepick.monglepickbackend.domain.movie.entity;

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
 * 사용자 선호 장르 엔티티 — fav_genre 테이블 매핑.
 *
 * <p>사용자가 선호하는 영화 장르를 우선순위와 함께 저장한다.
 * 온보딩(이상형 월드컵) 또는 프로필 설정에서 수집된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code userId} — 사용자 ID</li>
 *   <li>{@code genreName} — 장르명 (예: "액션", "로맨스")</li>
 *   <li>{@code priority} — 우선순위 (0이 가장 높음)</li>
 * </ul>
 *
 * <h3>제약조건</h3>
 * <p>UNIQUE(user_id, genre_name) — 동일 사용자가 동일 장르를 중복 등록할 수 없다.</p>
 */
@Entity
@Table(
        name = "fav_genre",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "genre_name"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavGenre extends BaseTimeEntity {

    /** 선호 장르 레코드 고유 ID (BIGINT AUTO_INCREMENT PK) */
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
     * 장르명 (VARCHAR(50), NOT NULL).
     * 예: "액션", "코미디", "SF", "로맨스", "호러"
     */
    @Column(name = "genre_name", length = 50, nullable = false)
    private String genreName;

    /**
     * 우선순위.
     * 기본값: 0.
     * 낮은 숫자일수록 높은 우선순위를 의미한다.
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;
}
