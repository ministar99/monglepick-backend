package com.monglepick.monglepickbackend.domain.reward.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RewardResult record 단위 테스트.
 *
 * <p>2026-05-11 마일스톤 보너스 합산 정합성을 위해 신설된 bonuses 필드와 헬퍼
 * ({@code totalPoints}, {@code withBonuses}, {@code of(...,bonuses)}) 의 동작을 검증한다.
 * AttendanceService 와 RewardService 가 이 헬퍼에 의존해 응답을 조립하므로,
 * record 단위에서 EMPTY/null/재귀 합산 시나리오를 빠짐없이 보장한다.</p>
 */
@DisplayName("RewardResult — 마일스톤 보너스 합산 헬퍼 단위 테스트")
class RewardResultTest {

    @Nested
    @DisplayName("EMPTY 상수")
    class EmptyConstant {

        /**
         * EMPTY 는 earned=false, points=0, bonuses=빈 리스트.
         * 호출자가 EMPTY 를 받아도 즉시 bonuses().stream() 등을 호출할 수 있어야 한다 (NPE 방지).
         */
        @Test
        @DisplayName("EMPTY 는 earned=false, points=0, bonuses=빈 리스트")
        void emptyHasZeroPointsAndNoBonuses() {
            assertThat(RewardResult.EMPTY.earned()).isFalse();
            assertThat(RewardResult.EMPTY.points()).isZero();
            assertThat(RewardResult.EMPTY.bonuses()).isEmpty();
            assertThat(RewardResult.EMPTY.totalPoints()).isZero();
        }
    }

    @Nested
    @DisplayName("of(points, policyName) — 자식 보너스 없는 기본 팩토리")
    class SimpleOfFactory {

        @Test
        @DisplayName("points > 0 이면 earned=true, bonuses=빈 리스트")
        void positivePointsMarkEarned() {
            RewardResult r = RewardResult.of(10, "출석 기본");
            assertThat(r.earned()).isTrue();
            assertThat(r.points()).isEqualTo(10);
            assertThat(r.policyName()).isEqualTo("출석 기본");
            assertThat(r.bonuses()).isEmpty();
            assertThat(r.totalPoints()).isEqualTo(10);
        }

        @Test
        @DisplayName("points = 0 이면 earned=false (지급 무산 결과)")
        void zeroPointsNotEarned() {
            RewardResult r = RewardResult.of(0, "출석 기본");
            assertThat(r.earned()).isFalse();
            assertThat(r.points()).isZero();
            assertThat(r.totalPoints()).isZero();
        }
    }

    @Nested
    @DisplayName("of(points, policyName, actionType, bonuses) — 자식 보너스 누적 팩토리")
    class BonusesOfFactory {

        @Test
        @DisplayName("bonuses 가 null 이면 빈 리스트로 정규화된다 (NPE 방지)")
        void nullBonusesNormalizedToEmptyList() {
            RewardResult r = RewardResult.of(10, "출석 기본", "ATTENDANCE_BASE", null);
            assertThat(r.bonuses()).isNotNull().isEmpty();
            assertThat(r.totalPoints()).isEqualTo(10);
        }

        @Test
        @DisplayName("자식 보너스 1건이 있으면 totalPoints 가 본인 + 보너스 합")
        void singleBonusAccumulated() {
            RewardResult bonus = RewardResult.of(50, "7일 연속 출석", "ATTENDANCE_STREAK_7", List.of());
            RewardResult r = RewardResult.of(10, "출석 기본", "ATTENDANCE_BASE", List.of(bonus));
            assertThat(r.points()).isEqualTo(10);
            assertThat(r.totalPoints()).isEqualTo(60);
            assertThat(r.bonuses()).hasSize(1);
            assertThat(r.bonuses().get(0).actionType()).isEqualTo("ATTENDANCE_STREAK_7");
        }

        @Test
        @DisplayName("자식 보너스 다건의 합산 — 30일 streak (10 + 300 + 100 = 410)")
        void multipleBonusesAccumulated() {
            RewardResult streak30 = RewardResult.of(300, "30일 연속 출석", "ATTENDANCE_STREAK_30", List.of());
            RewardResult streak15 = RewardResult.of(100, "15일 연속 출석", "ATTENDANCE_STREAK_15", List.of());
            RewardResult r = RewardResult.of(10, "출석 기본", "ATTENDANCE_BASE", List.of(streak30, streak15));
            assertThat(r.totalPoints()).isEqualTo(410);
        }

        @Test
        @DisplayName("자식 보너스가 또 자식을 가져도 재귀 합산이 정확하다 (안전 일반화)")
        void recursiveBonusesAccumulated() {
            RewardResult grandchild = RewardResult.of(5, "조부 보너스", "G_BONUS", List.of());
            RewardResult child = RewardResult.of(20, "자식 보너스", "C_BONUS", List.of(grandchild));
            RewardResult root = RewardResult.of(10, "루트", "ROOT", List.of(child));
            assertThat(root.totalPoints()).isEqualTo(35);
        }
    }

    @Nested
    @DisplayName("withBonuses — 본인 필드 보존 + bonuses 교체")
    class WithBonuses {

        @Test
        @DisplayName("withBonuses 는 본인 필드는 그대로 두고 bonuses 만 교체한 새 instance 반환")
        void preservesSelfReplacesBonuses() {
            RewardResult original = RewardResult.of(10, "출석 기본");
            RewardResult bonus = RewardResult.of(50, "7일 연속", "ATTENDANCE_STREAK_7", List.of());
            RewardResult updated = original.withBonuses(List.of(bonus));

            /* 본인 필드는 그대로 */
            assertThat(updated.earned()).isEqualTo(original.earned());
            assertThat(updated.points()).isEqualTo(original.points());
            assertThat(updated.policyName()).isEqualTo(original.policyName());

            /* bonuses 만 교체 */
            assertThat(updated.bonuses()).hasSize(1);
            assertThat(updated.totalPoints()).isEqualTo(60);

            /* 원본은 불변 — record 동일성 보장 */
            assertThat(original.bonuses()).isEmpty();
        }

        @Test
        @DisplayName("withBonuses(null) 도 빈 리스트로 정규화된다")
        void nullSafe() {
            RewardResult r = RewardResult.of(10, "출석 기본").withBonuses(null);
            assertThat(r.bonuses()).isNotNull().isEmpty();
        }
    }
}
