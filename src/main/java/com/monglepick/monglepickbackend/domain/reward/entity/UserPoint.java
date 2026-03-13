package com.monglepick.monglepickbackend.domain.reward.entity;

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

import java.time.LocalDate;

/**
 * 사용자 포인트 엔티티 — user_points 테이블 매핑.
 *
 * <p>사용자의 포인트 잔액 및 등급 정보를 관리한다.
 * 각 사용자당 하나의 포인트 레코드만 존재한다 (user_id UNIQUE).</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code userId} — 사용자 ID (FK → users.user_id, UNIQUE)</li>
 *   <li>{@code pointHave} — 현재 보유 포인트</li>
 *   <li>{@code totalEarned} — 누적 획득 포인트</li>
 *   <li>{@code dailyEarned} — 오늘 획득 포인트 (일일 한도 관리용)</li>
 *   <li>{@code dailyReset} — 일일 리셋 기준일 (DATE)</li>
 *   <li>{@code userGrade} — 사용자 등급 (BRONZE, SILVER, GOLD, PLATINUM)</li>
 * </ul>
 */
@Entity
@Table(
        name = "user_points",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPoint extends BaseTimeEntity {

    /** 포인트 레코드 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    /**
     * 사용자 ID (VARCHAR(50), NOT NULL, UNIQUE).
     * users.user_id를 참조한다.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /**
     * 현재 보유 포인트.
     * 기본값: 0.
     */
    @Column(name = "point_have")
    @Builder.Default
    private Integer pointHave = 0;

    /**
     * 누적 획득 포인트.
     * 기본값: 0.
     * 가입 이후 전체 획득 포인트 합산.
     */
    @Column(name = "total_earned")
    @Builder.Default
    private Integer totalEarned = 0;

    /**
     * 오늘 획득 포인트.
     * 기본값: 0.
     * 일일 포인트 획득 한도 관리에 사용된다.
     */
    @Column(name = "daily_earned")
    @Builder.Default
    private Integer dailyEarned = 0;

    /**
     * 일일 리셋 기준일.
     * dailyEarned가 마지막으로 0으로 초기화된 날짜.
     * 날짜가 바뀌면 dailyEarned를 0으로 리셋한다.
     */
    @Column(name = "daily_reset")
    private LocalDate dailyReset;

    /**
     * 사용자 등급 (최대 20자).
     * 기본값: "BRONZE".
     * 누적 포인트에 따라 BRONZE → SILVER → GOLD → PLATINUM 으로 승급.
     */
    @Column(name = "user_grade", length = 20)
    @Builder.Default
    private String userGrade = "BRONZE";
}
