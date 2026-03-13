package com.monglepick.monglepickbackend.domain.user.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 선호도 엔티티 — user_preferences 테이블 매핑.
 *
 * <p>사용자의 영화 취향/선호 정보를 저장한다.
 * 온보딩 과정(이상형 월드컵 등)이나 대화를 통해 수집된 선호 정보를 누적한다.
 * 각 사용자당 하나의 선호도 레코드만 존재한다 (user_id UNIQUE).</p>
 *
 * <h3>선호 필드 (모두 JSON 배열)</h3>
 * <ul>
 *   <li>{@code preferredGenres} — 선호 장르 (예: ["액션", "SF", "코미디"])</li>
 *   <li>{@code preferredMoods} — 선호 분위기/무드 (예: ["힐링", "감동"])</li>
 *   <li>{@code preferredDirectors} — 선호 감독 (예: ["봉준호", "크리스토퍼 놀란"])</li>
 *   <li>{@code preferredActors} — 선호 배우 (예: ["송강호", "마동석"])</li>
 *   <li>{@code preferredEras} — 선호 시대/연대 (예: ["2020s", "1990s"])</li>
 *   <li>{@code excludedGenres} — 제외할 장르 (예: ["호러", "스릴러"])</li>
 *   <li>{@code preferredPlatforms} — 선호 OTT 플랫폼 (예: ["넷플릭스", "왓챠"])</li>
 *   <li>{@code extraPreferences} — 기타 선호 정보 (JSON 객체)</li>
 * </ul>
 */
@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPreference extends BaseTimeEntity {

    /** 선호도 레코드 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 엔티티와의 1:1 관계.
     * user_preferences.user_id → users.user_id FK (ON DELETE CASCADE).
     * UNIQUE 제약으로 사용자당 하나의 선호도 레코드만 허용된다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 선호 장르 목록 (JSON 배열).
     * 예: ["액션", "SF", "코미디"]
     */
    @Column(name = "preferred_genres", columnDefinition = "json")
    private String preferredGenres;

    /**
     * 선호 분위기/무드 태그 목록 (JSON 배열).
     * 예: ["힐링", "감동", "따뜻"]
     */
    @Column(name = "preferred_moods", columnDefinition = "json")
    private String preferredMoods;

    /**
     * 선호 감독 목록 (JSON 배열).
     * 예: ["봉준호", "크리스토퍼 놀란"]
     */
    @Column(name = "preferred_directors", columnDefinition = "json")
    private String preferredDirectors;

    /**
     * 선호 배우 목록 (JSON 배열).
     * 예: ["송강호", "마동석"]
     */
    @Column(name = "preferred_actors", columnDefinition = "json")
    private String preferredActors;

    /**
     * 선호 시대/연대 목록 (JSON 배열).
     * 예: ["2020s", "1990s", "classic"]
     */
    @Column(name = "preferred_eras", columnDefinition = "json")
    private String preferredEras;

    /**
     * 제외할 장르 목록 (JSON 배열).
     * 이 장르에 해당하는 영화는 추천에서 제외된다.
     * 예: ["호러", "스릴러"]
     */
    @Column(name = "excluded_genres", columnDefinition = "json")
    private String excludedGenres;

    /**
     * 선호 OTT 플랫폼 목록 (JSON 배열).
     * 예: ["넷플릭스", "왓챠", "디즈니플러스"]
     */
    @Column(name = "preferred_platforms", columnDefinition = "json")
    private String preferredPlatforms;

    /**
     * 선호 관람등급 (문자열).
     * 예: "전체관람가", "15세이상관람가"
     */
    @Column(name = "preferred_certification", length = 50)
    private String preferredCertification;

    /**
     * 기타 선호 정보 (JSON 객체).
     * 구조화되지 않은 추가 선호 데이터를 자유롭게 저장한다.
     * 예: {"preferred_length": "short", "subtitles": true}
     */
    @Column(name = "extra_preferences", columnDefinition = "json")
    private String extraPreferences;
}
