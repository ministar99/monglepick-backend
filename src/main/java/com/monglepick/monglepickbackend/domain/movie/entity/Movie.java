package com.monglepick.monglepickbackend.domain.movie.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영화 정보 엔티티 — movies 테이블 매핑.
 *
 * <p>TMDB, KOBIS, KMDb 등 다양한 소스에서 수집된 영화 메타데이터를 저장한다.
 * PK는 VARCHAR(50)이며 TMDB ID, KOBIS 영화코드, KMDb ID 등이 사용된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code movieId} — 영화 고유 ID (TMDB/KOBIS/KMDb 소스별 ID, VARCHAR(50) PK)</li>
 *   <li>{@code title} — 한국어 제목 (필수)</li>
 *   <li>{@code titleEn} — 영문 제목</li>
 *   <li>{@code genres} — 장르 목록 (JSON 배열, 예: ["액션", "SF"])</li>
 *   <li>{@code cast} — 주연 배우 목록 (JSON 배열)</li>
 *   <li>{@code source} — 데이터 출처 (tmdb, kobis, kmdb 등)</li>
 * </ul>
 *
 * <h3>KOBIS 연동 필드</h3>
 * <ul>
 *   <li>{@code kobisMovieCd} — 영진위 영화코드</li>
 *   <li>{@code salesAcc} — 누적매출액</li>
 *   <li>{@code audienceCount} — 누적관객수</li>
 *   <li>{@code screenCount} — 스크린수</li>
 *   <li>{@code kobisWatchGrade} — 관람등급</li>
 *   <li>{@code kobisOpenDt} — 개봉일 (YYYYMMDD 형식 문자열)</li>
 * </ul>
 *
 * <h3>KMDb 연동 필드</h3>
 * <ul>
 *   <li>{@code kmdbId} — KMDb 영화 ID</li>
 *   <li>{@code awards} — 수상 이력 (텍스트)</li>
 *   <li>{@code filmingLocation} — 촬영지 정보 (텍스트)</li>
 * </ul>
 */
@Entity
@Table(name = "movies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Movie extends BaseTimeEntity {

    /**
     * 영화 고유 ID (PK).
     * TMDB ID, KOBIS 영화코드, KMDb ID 등 소스별로 다른 형식의 ID가 사용된다.
     * AUTO_INCREMENT가 아닌 VARCHAR(50) 문자열 PK이다.
     */
    @Id
    @Column(name = "movie_id", length = 50, nullable = false)
    private String movieId;

    /** 한국어 영화 제목 (필수, 최대 500자) */
    @Column(name = "title", length = 500, nullable = false)
    private String title;

    /** 영문 영화 제목 (선택, 최대 500자) */
    @Column(name = "title_en", length = 500)
    private String titleEn;

    /** 포스터 이미지 경로 (TMDB 상대경로 또는 전체 URL) */
    @Column(name = "poster_path", length = 500)
    private String posterPath;

    /** 배경 이미지 경로 (TMDB 상대경로 또는 전체 URL) */
    @Column(name = "backdrop_path", length = 500)
    private String backdropPath;

    /** 개봉 연도 (예: 2024) */
    @Column(name = "release_year")
    private Integer releaseYear;

    /** 상영시간 (분 단위) */
    @Column(name = "runtime")
    private Integer runtime;

    /** 평균 평점 (TMDB 기준, 0.0~10.0) */
    @Column(name = "rating")
    private Float rating;

    /** 투표(평가) 수 */
    @Column(name = "vote_count")
    private Integer voteCount;

    /** 인기도 점수 (TMDB 인기도 지표) */
    @Column(name = "popularity_score")
    private Float popularityScore;

    /**
     * 장르 목록 (JSON 배열).
     * 예: ["액션", "SF", "모험"]
     * MySQL JSON 타입 컬럼으로 저장된다.
     */
    @Column(name = "genres", columnDefinition = "json")
    private String genres;

    /** 감독 이름 (최대 200자) */
    @Column(name = "director", length = 200)
    private String director;

    /**
     * 주연 배우 목록 (JSON 배열).
     * 예: ["배우1", "배우2", "배우3"]
     * MySQL JSON 타입 컬럼으로 저장된다.
     */
    @Column(name = "cast", columnDefinition = "json")
    private String cast;

    /** 관람 등급 (예: "15세이상관람가", "전체관람가", "R") */
    @Column(name = "certification", length = 50)
    private String certification;

    /** 트레일러 URL (YouTube 등 외부 링크) */
    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    /** 영화 줄거리/개요 (TEXT 타입) */
    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    /** 영화 태그라인/캐치프레이즈 (최대 500자) */
    @Column(name = "tagline", length = 500)
    private String tagline;

    /** IMDB 영화 ID (예: "tt1234567") */
    @Column(name = "imdb_id", length = 20)
    private String imdbId;

    /** 원본 언어 코드 (예: "ko", "en", "ja") */
    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    /** 영화 컬렉션/시리즈 이름 (예: "마블 시네마틱 유니버스") */
    @Column(name = "collection_name", length = 200)
    private String collectionName;

    /** KOBIS(영화진흥위원회) 영화코드 */
    @Column(name = "kobis_movie_cd", length = 20)
    private String kobisMovieCd;

    /** KOBIS 누적매출액 (원 단위) */
    @Column(name = "sales_acc")
    private Long salesAcc;

    /** KOBIS 누적관객수 */
    @Column(name = "audience_count")
    private Long audienceCount;

    /** 스크린수 */
    @Column(name = "screen_count")
    private Integer screenCount;

    /** KOBIS 관람등급 */
    @Column(name = "kobis_watch_grade", length = 50)
    private String kobisWatchGrade;

    /** KOBIS 개봉일 (YYYYMMDD 형식 문자열) */
    @Column(name = "kobis_open_dt", length = 10)
    private String kobisOpenDt;

    /** KMDb(한국영화데이터베이스) 영화 ID */
    @Column(name = "kmdb_id", length = 50)
    private String kmdbId;

    /** 수상 이력 (자유 텍스트) */
    @Column(name = "awards", columnDefinition = "TEXT")
    private String awards;

    /** 촬영지 정보 (자유 텍스트) */
    @Column(name = "filming_location", columnDefinition = "TEXT")
    private String filmingLocation;

    /** 데이터 출처 (tmdb, kobis, kmdb, kaggle 등) */
    @Column(name = "source", length = 20)
    private String source;
}
