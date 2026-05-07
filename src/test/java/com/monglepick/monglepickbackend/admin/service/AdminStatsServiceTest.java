package com.monglepick.monglepickbackend.admin.service;

import com.monglepick.monglepickbackend.admin.dto.StatsDto.BehaviorResponse;
import com.monglepick.monglepickbackend.admin.dto.StatsDto.RetentionResponse;
import com.monglepick.monglepickbackend.admin.repository.AdminChatSessionRepository;
import com.monglepick.monglepickbackend.admin.dto.StatsDto.OverviewResponse;
import com.monglepick.monglepickbackend.admin.dto.StatsDto.TrendsResponse;
import com.monglepick.monglepickbackend.domain.community.entity.PostStatus;
import com.monglepick.monglepickbackend.domain.community.mapper.PostMapper;
import com.monglepick.monglepickbackend.domain.recommendation.repository.EventLogRepository;
import com.monglepick.monglepickbackend.domain.review.mapper.ReviewMapper;
import com.monglepick.monglepickbackend.domain.search.repository.SearchHistoryRepository;
import com.monglepick.monglepickbackend.domain.user.entity.User;
import com.monglepick.monglepickbackend.domain.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminStatsService} 서비스 통계 단위 테스트.
 *
 * <p>회귀 배경:</p>
 * <ul>
 *   <li>서비스 통계 KPI 카드가 기간 필터(7/30/90일)와 분리되어 움직이지 않던 문제</li>
 *   <li>신규 가입 카드와 차트의 집계 기준이 달라 같은 기간인데 값이 맞지 않던 문제</li>
 *   <li>일별 집계가 자정 경계에서 중복될 수 있던 문제</li>
 * </ul>
 *
 * <p>회귀 차단:</p>
 * <ul>
 *   <li>{@link AdminStatsService#getOverview(String)} 가 기간별 일간 추이 합계/최댓값으로 KPI 를 계산하는지</li>
 *   <li>{@link AdminStatsService#getTrends(String)} 가 KST 기준 정확한 날짜 개수만 반환하는지</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Mock
    private UserMapper userMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private AdminChatSessionRepository adminChatSessionRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    @DisplayName("getOverview(period) 는 선택 기간의 일별 추이 기준으로 DAU/MAU/신규 가입을 계산한다")
    void getOverview_alignsKpisWithSelectedPeriod() {
        when(userMapper.countByLastLoginAtBetween(any(), any()))
                .thenReturn(12L, 8L, 15L, 11L, 9L, 10L, 14L);
        when(userMapper.countByCreatedAtBetween(any(), any()))
                .thenReturn(1L, 0L, 2L, 0L, 1L, 3L, 2L);
        when(reviewMapper.count()).thenReturn(128L);
        when(reviewMapper.findAverageRating()).thenReturn(4.356d);
        when(postMapper.countByStatus(eq(PostStatus.PUBLISHED.name()), isNull())).thenReturn(42L);

        OverviewResponse response = adminStatsService.getOverview("7d");

        assertThat(response.dau()).isEqualTo(15L);
        assertThat(response.mau()).isEqualTo(79L);
        assertThat(response.newUsersWeek()).isEqualTo(9L);
        assertThat(response.newUsers()).isEqualTo(9L);
        assertThat(response.totalReviews()).isEqualTo(128L);
        assertThat(response.avgRating()).isEqualTo(4.36d);
        assertThat(response.totalPosts()).isEqualTo(42L);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userMapper, times(7)).countByLastLoginAtBetween(startCaptor.capture(), endCaptor.capture());

        List<LocalDateTime> starts = startCaptor.getAllValues();
        List<LocalDateTime> ends = endCaptor.getAllValues();
        LocalDate today = LocalDate.now(KST);

        assertThat(starts).isSorted();
        assertThat(starts.getFirst()).isEqualTo(today.minusDays(6).atStartOfDay());
        assertThat(ends.getLast()).isEqualTo(today.plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("getTrends(period) 는 KST 기준 기간 길이만큼 날짜 오름차순 추이를 반환한다")
    void getTrends_returnsExactlySelectedPeriod() {
        when(userMapper.countByLastLoginAtBetween(any(), any())).thenReturn(3L);
        when(userMapper.countByCreatedAtBetween(any(), any())).thenReturn(1L);
        when(reviewMapper.countByCreatedAtBetween(any(), any())).thenReturn(2L);
        when(postMapper.countByStatusAndCreatedAtBetween(eq(PostStatus.PUBLISHED.name()), any(), any()))
                .thenReturn(4L);

        TrendsResponse response = adminStatsService.getTrends("30d");

        LocalDate today = LocalDate.now(KST);

        assertThat(response.trends()).hasSize(30);
        assertThat(response.trends().getFirst().date()).isEqualTo(today.minusDays(29).format(DATE_FMT));
        assertThat(response.trends().getLast().date()).isEqualTo(today.format(DATE_FMT));
        assertThat(response.trends())
                .allSatisfy(item -> {
                    assertThat(item.dau()).isEqualTo(3L);
                    assertThat(item.newUsers()).isEqualTo(1L);
                    assertThat(item.reviews()).isEqualTo(2L);
                    assertThat(item.posts()).isEqualTo(4L);
                });
    }

    @Test
    @DisplayName("getUserBehavior(period) 는 선택 기간 기준 장르 분포와 시간대 분포를 집계한다")
    void getUserBehavior_appliesSelectedPeriod() {
        when(reviewMapper.countGenresByCreatedAtBetween(any(), any())).thenReturn(List.of(
                Map.of("genre", "액션", "cnt", 5L),
                Map.of("genre", "드라마", "cnt", 3L)
        ));
        when(userMapper.countLastLoginGroupedByHourBetween(any(), any())).thenReturn(List.of(
                Map.of("hour", 9, "cnt", 2L),
                Map.of("hour", 21, "cnt", 4L)
        ));

        BehaviorResponse response = adminStatsService.getUserBehavior("7d");

        assertThat(response.genrePreferences()).hasSize(2);
        assertThat(response.genrePreferences().getFirst().genre()).isEqualTo("액션");
        assertThat(response.genrePreferences().getFirst().count()).isEqualTo(5L);
        assertThat(response.genrePreferences().getFirst().percentage()).isEqualTo(62.5d);
        assertThat(response.genrePreferences().get(1).genre()).isEqualTo("드라마");
        assertThat(response.genrePreferences().get(1).percentage()).isEqualTo(37.5d);

        assertThat(response.hourlyActivity()).hasSize(24);
        assertThat(response.hourlyActivity().get(9).count()).isEqualTo(2L);
        assertThat(response.hourlyActivity().get(21).count()).isEqualTo(4L);
        assertThat(response.hourlyActivity().get(0).count()).isZero();

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reviewMapper).countGenresByCreatedAtBetween(startCaptor.capture(), endCaptor.capture());

        LocalDate today = LocalDate.now(KST);
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(6).atStartOfDay());
        assertThat(endCaptor.getValue()).isEqualTo(today.plusDays(1).atStartOfDay());
        verify(userMapper).countLastLoginGroupedByHourBetween(startCaptor.getValue(), endCaptor.getValue());
    }

    @Test
    @DisplayName("getRetention(cohortWeeks, horizonWeeks) 는 완료된 주간 코호트와 실제 활동 로그 기준 유지율을 계산한다")
    void getRetention_usesDedicatedFiltersAndActualActivity() {
        LocalDate currentWeekStart = LocalDate.now(KST)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        User oldestCohortUser = User.builder().userId("user-a").build();
        User newestCohortUser = User.builder().userId("user-b").build();

        when(userMapper.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(oldestCohortUser))
                .thenReturn(List.of(newestCohortUser));

        when(reviewMapper.findDistinctUserIdsByUserIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of("user-a"))
                .thenReturn(List.of());
        when(eventLogRepository.findDistinctUserIdsByUserIdInAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of())
                .thenReturn(List.of());
        when(searchHistoryRepository.findDistinctUserIdsByUserIdInAndSearchedAtBetween(any(), any(), any()))
                .thenReturn(List.of())
                .thenReturn(List.of());
        when(adminChatSessionRepository.findDistinctUserIdsByUserIdInAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of())
                .thenReturn(List.of());

        RetentionResponse response = adminStatsService.getRetention(2, 1);

        assertThat(response.cohorts()).hasSize(2);
        assertThat(response.cohorts().getFirst().cohortWeek())
                .isEqualTo(currentWeekStart.minusWeeks(2).getYear() + "-W"
                        + String.format("%02d", currentWeekStart.minusWeeks(2)
                        .get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)));
        assertThat(response.cohorts().getFirst().cohortSize()).isEqualTo(1L);
        assertThat(response.cohorts().getFirst().retentionRates()).containsExactly(100.0d);

        assertThat(response.cohorts().get(1).cohortSize()).isEqualTo(1L);
        assertThat(response.cohorts().get(1).retentionRates()).isEmpty();

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userMapper, times(2)).findByCreatedAtBetween(startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getAllValues().getFirst()).isEqualTo(currentWeekStart.minusWeeks(2).atStartOfDay());
        assertThat(endCaptor.getAllValues().getFirst()).isEqualTo(currentWeekStart.minusWeeks(1).atStartOfDay());
        assertThat(startCaptor.getAllValues().get(1)).isEqualTo(currentWeekStart.minusWeeks(1).atStartOfDay());
        assertThat(endCaptor.getAllValues().get(1)).isEqualTo(currentWeekStart.atStartOfDay());
    }
}
