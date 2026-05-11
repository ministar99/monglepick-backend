package com.monglepick.monglepickbackend.domain.reward.service;

import com.monglepick.monglepickbackend.domain.reward.dto.PointDto.AttendanceResponse;
import com.monglepick.monglepickbackend.domain.reward.dto.PointDto.AttendanceStatusResponse;
import com.monglepick.monglepickbackend.domain.reward.dto.RewardResult;
import com.monglepick.monglepickbackend.domain.reward.entity.UserActivityProgress;
import com.monglepick.monglepickbackend.domain.reward.entity.UserAttendance;
import com.monglepick.monglepickbackend.domain.reward.repository.UserActivityProgressRepository;
import com.monglepick.monglepickbackend.domain.reward.repository.UserAttendanceRepository;
import com.monglepick.monglepickbackend.global.exception.BusinessException;
import com.monglepick.monglepickbackend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AttendanceService 단위 테스트.
 *
 * <p>2026-05-11 회귀 수정 — 마일스톤 보너스 합산 + 이전달 조회 신설을 검증한다.
 * 핵심 정합성 시나리오:</p>
 * <ul>
 *   <li>{@code checkIn} 정상 + 7일/30일 streak 보너스가 RewardResult.bonuses 에서
 *       AttendanceResponse 의 totalEarned/baseEarned/bonuses 로 올바르게 합산된다</li>
 *   <li>RewardService 가 EMPTY 를 반환해도 응답 totalEarned=0 + bonuses=빈 리스트로 안전</li>
 *   <li>{@code getStatus(null)} 은 현재 달, {@code getStatus(YearMonth)} 는 지정 달의
 *       monthlyDates 만 반환. 통계 필드는 조회 달과 무관하게 사용자 현재 상태</li>
 *   <li>미래 달 요청 시 INVALID_INPUT BusinessException</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService — 마일스톤 합산 + 이전달 조회")
class AttendanceServiceTest {

    @Mock
    private UserAttendanceRepository attendanceRepository;

    @Mock
    private RewardService rewardService;

    @Mock
    private UserActivityProgressRepository progressRepo;

    @InjectMocks
    private AttendanceService attendanceService;

    private static final String USER_ID = "user-1";

    /**
     * checkIn 정상 흐름은 {@code progressRepo.findByUserIdAndActionType} 을 호출하므로 stub 이
     * 필요하지만 alreadyAttended/getStatus 케이스는 호출하지 않는다. Mockito strict mode 의
     * UnnecessaryStubbingException 을 피하기 위해 각 정상 케이스에서 직접 stub 한다.
     */
    private void stubProgressEmpty() {
        when(progressRepo.findByUserIdAndActionType(USER_ID, "ATTENDANCE_BASE"))
                .thenReturn(Optional.empty());
    }

    // ──────────────────────────────────────────────
    // checkIn — 마일스톤 보너스 합산 (Phase 1)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("checkIn — RewardService.bonuses 가 응답에 합산된다")
    class CheckInBonusAggregation {

        @Test
        @DisplayName("streak=1 — 자식 보너스 없음. baseEarned=10, totalEarned=10, bonuses=빈 리스트")
        void streakOneBaseOnly() {
            stubProgressEmpty();
            /* 어제 출석 기록 없음 → streak=1 (신규 출석) */
            when(attendanceRepository.findByUserIdAndCheckDate(eq(USER_ID), any()))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.empty());

            /* RewardService 가 자식 보너스 없는 RewardResult 반환 */
            RewardResult rewardResult = RewardResult.of(10, "출석 기본", "ATTENDANCE_BASE", List.of());
            when(rewardService.grantReward(eq(USER_ID), eq("ATTENDANCE_BASE"), anyString(), anyInt()))
                    .thenReturn(rewardResult);

            AttendanceResponse response = attendanceService.checkIn(USER_ID);

            assertThat(response.streakCount()).isEqualTo(1);
            assertThat(response.baseEarned()).isEqualTo(10);
            assertThat(response.totalEarned()).isEqualTo(10);
            assertThat(response.earnedPoints()).isEqualTo(10);   // Deprecated 호환
            assertThat(response.bonuses()).isEmpty();
        }

        @Test
        @DisplayName("streak=7 — STREAK_7 보너스 1건이 응답 bonuses 에 포함. totalEarned=60")
        void streakSevenAddsBonus() {
            stubProgressEmpty();
            /* 어제 출석 (streak=6) → 오늘 streak=7 */
            UserAttendance yesterday = UserAttendance.builder()
                    .userId(USER_ID)
                    .checkDate(LocalDate.now().minusDays(1))
                    .streakCount(6)
                    .build();
            when(attendanceRepository.findByUserIdAndCheckDate(eq(USER_ID), any()))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.of(yesterday));

            /* RewardService 가 자식 보너스 1건 포함 RewardResult 반환 */
            RewardResult streak7 = RewardResult.of(50, "7일 연속 출석", "ATTENDANCE_STREAK_7", List.of());
            RewardResult rewardResult = RewardResult.of(10, "출석 기본", "ATTENDANCE_BASE", List.of(streak7));
            when(rewardService.grantReward(eq(USER_ID), eq("ATTENDANCE_BASE"), anyString(), anyInt()))
                    .thenReturn(rewardResult);

            AttendanceResponse response = attendanceService.checkIn(USER_ID);

            assertThat(response.streakCount()).isEqualTo(7);
            assertThat(response.baseEarned()).isEqualTo(10);
            assertThat(response.totalEarned()).isEqualTo(60);
            assertThat(response.earnedPoints()).isEqualTo(60);
            assertThat(response.bonuses()).hasSize(1);
            assertThat(response.bonuses().get(0).actionType()).isEqualTo("ATTENDANCE_STREAK_7");
            assertThat(response.bonuses().get(0).activityName()).isEqualTo("7일 연속 출석");
            assertThat(response.bonuses().get(0).points()).isEqualTo(50);
        }

        @Test
        @DisplayName("streak=30 — STREAK_30 + STREAK_15(30%15=0) 합산. totalEarned=410")
        void streakThirtyAddsMultipleBonuses() {
            stubProgressEmpty();
            UserAttendance yesterday = UserAttendance.builder()
                    .userId(USER_ID)
                    .checkDate(LocalDate.now().minusDays(1))
                    .streakCount(29)
                    .build();
            when(attendanceRepository.findByUserIdAndCheckDate(eq(USER_ID), any()))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.of(yesterday));

            /* RewardService 가 STREAK_30, STREAK_15 두 보너스 합쳐 반환 (30%7!=0 이므로 STREAK_7 제외) */
            RewardResult streak30 = RewardResult.of(300, "30일 연속 출석", "ATTENDANCE_STREAK_30", List.of());
            RewardResult streak15 = RewardResult.of(100, "15일 연속 출석", "ATTENDANCE_STREAK_15", List.of());
            RewardResult rewardResult = RewardResult.of(
                    10, "출석 기본", "ATTENDANCE_BASE", List.of(streak30, streak15));
            when(rewardService.grantReward(eq(USER_ID), eq("ATTENDANCE_BASE"), anyString(), anyInt()))
                    .thenReturn(rewardResult);

            AttendanceResponse response = attendanceService.checkIn(USER_ID);

            assertThat(response.streakCount()).isEqualTo(30);
            assertThat(response.baseEarned()).isEqualTo(10);
            assertThat(response.totalEarned()).isEqualTo(410);
            assertThat(response.bonuses())
                    .extracting("actionType")
                    .containsExactly("ATTENDANCE_STREAK_30", "ATTENDANCE_STREAK_15");
        }

        @Test
        @DisplayName("RewardService 가 EMPTY 반환 — baseEarned=0, totalEarned=0, bonuses=빈 리스트")
        void emptyRewardYieldsZeroResponse() {
            stubProgressEmpty();
            when(attendanceRepository.findByUserIdAndCheckDate(eq(USER_ID), any()))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.empty());
            when(rewardService.grantReward(eq(USER_ID), eq("ATTENDANCE_BASE"), anyString(), anyInt()))
                    .thenReturn(RewardResult.EMPTY);

            AttendanceResponse response = attendanceService.checkIn(USER_ID);

            assertThat(response.baseEarned()).isZero();
            assertThat(response.totalEarned()).isZero();
            assertThat(response.earnedPoints()).isZero();
            assertThat(response.bonuses()).isEmpty();
        }

        @Test
        @DisplayName("오늘 이미 출석함 — ALREADY_ATTENDED 예외, grantReward 미호출")
        void alreadyAttendedThrows() {
            UserAttendance today = UserAttendance.builder()
                    .userId(USER_ID)
                    .checkDate(LocalDate.now())
                    .streakCount(1)
                    .build();
            when(attendanceRepository.findByUserIdAndCheckDate(eq(USER_ID), any()))
                    .thenReturn(Optional.of(today));

            assertThatThrownBy(() -> attendanceService.checkIn(USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ATTENDED);
            verify(rewardService, times(0))
                    .grantReward(anyString(), anyString(), anyString(), anyInt());
        }
    }

    // ──────────────────────────────────────────────
    // getStatus — 이전달 조회 (Phase 2)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("getStatus — YearMonth 파라미터로 대상 달 한정")
    class GetStatusByMonth {

        @Test
        @DisplayName("getStatus(null) — 현재 달의 monthlyDates 와 month=현재 YYYY-MM 반환")
        void nullTargetReturnsCurrentMonth() {
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

            UserAttendance latest = UserAttendance.builder()
                    .userId(USER_ID).checkDate(today).streakCount(3).build();
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.of(latest));
            when(attendanceRepository.countByUserId(USER_ID)).thenReturn(5L);
            when(attendanceRepository.findByUserIdAndCheckDateBetween(USER_ID, monthStart, monthEnd))
                    .thenReturn(List.of(latest));

            AttendanceStatusResponse response = attendanceService.getStatus(USER_ID, null);

            assertThat(response.currentStreak()).isEqualTo(3);
            assertThat(response.totalDays()).isEqualTo(5);
            assertThat(response.checkedToday()).isTrue();
            assertThat(response.monthlyDates()).containsExactly(today);
            assertThat(response.month()).isEqualTo(YearMonth.from(today).toString());
        }

        @Test
        @DisplayName("getStatus(과거 YearMonth) — 지정 달의 monthlyDates 만 반환, 통계는 사용자 현재 상태")
        void pastMonthLimitsMonthlyDatesOnly() {
            LocalDate today = LocalDate.now();
            YearMonth target = YearMonth.from(today).minusMonths(1);
            LocalDate monthStart = target.atDay(1);
            LocalDate monthEnd = target.atEndOfMonth();

            /* 사용자 최근 활동 — 오늘 출석 (현재 streak 유효) */
            UserAttendance latest = UserAttendance.builder()
                    .userId(USER_ID).checkDate(today).streakCount(2).build();
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.of(latest));
            when(attendanceRepository.countByUserId(USER_ID)).thenReturn(8L);

            /* 지정 달의 출석 기록 2건 */
            UserAttendance pastDay1 = UserAttendance.builder()
                    .userId(USER_ID).checkDate(monthStart.plusDays(2)).streakCount(1).build();
            UserAttendance pastDay2 = UserAttendance.builder()
                    .userId(USER_ID).checkDate(monthStart.plusDays(10)).streakCount(1).build();
            when(attendanceRepository.findByUserIdAndCheckDateBetween(USER_ID, monthStart, monthEnd))
                    .thenReturn(List.of(pastDay1, pastDay2));

            AttendanceStatusResponse response = attendanceService.getStatus(USER_ID, target);

            /* 통계는 현재 상태 그대로 — 대상 달과 무관 */
            assertThat(response.currentStreak()).isEqualTo(2);
            assertThat(response.totalDays()).isEqualTo(8);
            assertThat(response.checkedToday()).isTrue();
            /* monthlyDates 는 대상 달로 한정 */
            assertThat(response.monthlyDates())
                    .containsExactly(pastDay1.getCheckDate(), pastDay2.getCheckDate());
            assertThat(response.month()).isEqualTo(target.toString());
        }

        @Test
        @DisplayName("미래 YearMonth 는 INVALID_INPUT BusinessException")
        void futureMonthRejected() {
            YearMonth future = YearMonth.from(LocalDate.now()).plusMonths(1);

            assertThatThrownBy(() -> attendanceService.getStatus(USER_ID, future))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("연속 출석 없음 — 마지막 출석이 그저께 → currentStreak=0")
        void brokenStreakReturnsZero() {
            LocalDate today = LocalDate.now();
            UserAttendance dayBeforeYesterday = UserAttendance.builder()
                    .userId(USER_ID).checkDate(today.minusDays(2)).streakCount(3).build();
            when(attendanceRepository.findTopByUserIdOrderByCheckDateDesc(USER_ID))
                    .thenReturn(Optional.of(dayBeforeYesterday));
            when(attendanceRepository.countByUserId(USER_ID)).thenReturn(1L);
            when(attendanceRepository.findByUserIdAndCheckDateBetween(
                    eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            AttendanceStatusResponse response = attendanceService.getStatus(USER_ID, null);

            assertThat(response.currentStreak()).isZero();
            assertThat(response.checkedToday()).isFalse();
        }
    }
}
