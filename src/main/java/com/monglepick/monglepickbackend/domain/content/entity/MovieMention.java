package com.monglepick.monglepickbackend.domain.content.entity;

import com.monglepick.monglepickbackend.domain.movie.entity.Movie;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 영화 언급 통계 엔티티 — movie_mentions 테이블 매핑.
 *
 * <p>커뮤니티, SNS, 뉴스 등 다양한 소스에서 영화가 언급된 횟수와
 * 감성 분석 결과를 기간별로 집계하여 저장한다.
 * 트렌드 분석 및 인기도 보정에 활용된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code movie} — 언급된 영화 (FK → movies.movie_id)</li>
 *   <li>{@code source} — 데이터 소스 (예: "community", "twitter", "news")</li>
 *   <li>{@code mentionCount} — 언급 횟수</li>
 *   <li>{@code sentimentAvg} — 평균 감성 점수 (-1.0 ~ 1.0)</li>
 *   <li>{@code periodStart} / {@code periodEnd} — 집계 기간</li>
 * </ul>
 *
 * <h3>UNIQUE 제약</h3>
 * <p>(movie_id, source, period_start)가 유니크하므로,
 * 같은 영화+소스+기간에 대해 하나의 집계 레코드만 존재한다.</p>
 */
@Entity
@Table(name = "movie_mentions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mention_movie_source_period", columnNames = {"movie_id", "source", "period_start"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MovieMention {

    /** 언급 통계 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 언급된 영화.
     * movie_mentions.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * 데이터 소스 (최대 50자).
     * 예: "community"(자체 커뮤니티), "twitter", "reddit", "news"
     */
    @Column(name = "source", length = 50, nullable = false)
    private String source;

    /** 집계 기간 내 언급 횟수 (기본값: 0) */
    @Column(name = "mention_count")
    @Builder.Default
    private Integer mentionCount = 0;

    /**
     * 평균 감성 점수 (-1.0 ~ 1.0).
     * -1.0: 매우 부정, 0.0: 중립, 1.0: 매우 긍정.
     * 감성 분석이 수행되지 않은 경우 NULL.
     */
    @Column(name = "sentiment_avg")
    private Float sentimentAvg;

    /** 집계 시작일 (필수) */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /** 집계 종료일 (필수) */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /** 레코드 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
