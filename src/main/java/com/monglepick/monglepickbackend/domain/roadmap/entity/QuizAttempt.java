package com.monglepick.monglepickbackend.domain.roadmap.entity;

import com.monglepick.monglepickbackend.domain.movie.entity.Movie;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 퀴즈 시도 엔티티 — quiz_attempts 테이블 매핑.
 *
 * <p>도장깨기 코스에서 사용자가 영화 관련 퀴즈에 도전한 기록을 저장한다.
 * 각 시도마다 문제, 사용자 답변, 정답, 정오 여부, 점수를 기록한다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 퀴즈 도전자 (FK → users.user_id)</li>
 *   <li>{@code courseId} — 코스 식별자 (roadmap_courses.course_id와 매핑)</li>
 *   <li>{@code movie} — 퀴즈 대상 영화 (FK → movies.movie_id)</li>
 *   <li>{@code question} — 퀴즈 문제 (TEXT)</li>
 *   <li>{@code userAnswer} — 사용자 답변 (TEXT)</li>
 *   <li>{@code correctAnswer} — 정답 (TEXT)</li>
 *   <li>{@code isCorrect} — 정답 여부 (필수)</li>
 *   <li>{@code score} — 획득 점수 (기본값: 0)</li>
 * </ul>
 */
@Entity
@Table(name = "quiz_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizAttempt {

    /** 퀴즈 시도 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 퀴즈 도전 사용자.
     * quiz_attempts.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 코스 식별자.
     * roadmap_courses.course_id 값과 매핑되는 문자열.
     * DDL상 VARCHAR(50)이며 FK 제약은 없지만 논리적으로 연결된다.
     */
    @Column(name = "course_id", length = 50, nullable = false)
    private String courseId;

    /**
     * 퀴즈 대상 영화.
     * quiz_attempts.movie_id → movies.movie_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** 퀴즈 문제 (TEXT, 필수) */
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    /** 사용자가 입력한 답변 (TEXT, 필수) */
    @Column(name = "user_answer", columnDefinition = "TEXT", nullable = false)
    private String userAnswer;

    /** 정답 (TEXT, 필수) */
    @Column(name = "correct_answer", columnDefinition = "TEXT", nullable = false)
    private String correctAnswer;

    /** 정답 여부 (필수) */
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    /** 획득 점수 (기본값: 0) */
    @Column(name = "score")
    @Builder.Default
    private Integer score = 0;

    /** 퀴즈 시도 시각 */
    @CreationTimestamp
    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;
}
