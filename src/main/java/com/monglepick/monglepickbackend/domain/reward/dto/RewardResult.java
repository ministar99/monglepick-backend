package com.monglepick.monglepickbackend.domain.reward.dto;

import java.util.List;

/**
 * 리워드 지급 결과 DTO.
 *
 * <p>{@code RewardService.grantReward()} 호출 후 지급 성공 여부와 지급된 포인트를
 * 호출자(ReviewService, PostService, AttendanceService 등)에 반환한다. 호출자는
 * 이 정보를 API 응답 DTO에 포함하여 클라이언트가 리워드 획득 알림을 표시할 수 있게 한다.</p>
 *
 * <h3>2026-05-11 — bonuses 필드 신설 (출석 마일스톤 보너스 합산)</h3>
 * <p>기존에는 본 record 가 단일 정책의 지급 결과만 표현했고, parent 정책이 발동시킨 자식
 * threshold 정책(예: {@code ATTENDANCE_BASE} → {@code ATTENDANCE_STREAK_7})의 지급은
 * {@code grantReward()} 내부에서 재귀 호출되며 결과가 버려졌다. 그 결과 출석 응답의
 * {@code earnedPoints} 가 기본 포인트(10P)만 담고 streak 보너스(50/100/300P)는 누락되어
 * 7일째 출석한 사용자에게 실제 60P 가 적립됐는데 UI 는 +10P 만 보여주는 정합성 결함이
 * 있었다.</p>
 *
 * <p>이제 {@link #bonuses} 필드에 자식 정책의 지급 결과를 누적 보관한다. 호출자는
 * {@link #totalPoints()} 로 본인 + 모든 자식 보너스를 합한 총 적립 포인트를 얻을 수 있고,
 * {@link #bonuses()} 로 개별 보너스를 순회해 UI 에 내역을 노출할 수 있다. 등급 승격
 * 보너스({@code GRADE_UP_*}) 도 동일 경로로 합산된다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * RewardResult result = rewardService.grantReward(userId, "ATTENDANCE_BASE", ref, 0);
 * int total = result.totalPoints();             // 기본 + 모든 자식 보너스 합
 * int base  = result.points();                  // 본인 정책의 지급액만
 * List<RewardResult> bonuses = result.bonuses(); // 자식 보너스 내역
 * }</pre>
 *
 * @param earned     리워드 지급 여부 (조건 미충족/비활성/예외 시 false)
 * @param points     본인 정책으로 지급된 포인트 (미지급 시 0). 자식 보너스는 포함하지 않는다.
 * @param policyName 정책 한국어 이름 (클라이언트 표시용, 예: "리뷰 작성", "출석 기본")
 * @param actionType 정책의 action_type 코드 (자식 보너스 표시 시 식별용, null 허용)
 * @param bonuses    이 지급에 연쇄된 자식/등급승격 보너스 결과 (없으면 빈 리스트, 절대 null 아님)
 */
public record RewardResult(
        boolean earned,
        int points,
        String policyName,
        String actionType,
        List<RewardResult> bonuses
) {
    /**
     * 리워드 미지급 상수 (정책 없음, 조건 미충족, 예외 등).
     *
     * <p>bonuses 는 빈 불변 리스트로 초기화되어 null 안전성을 보장한다. 호출자가
     * {@code result.bonuses().stream()...} 처럼 즉시 순회해도 NPE 가 발생하지 않는다.</p>
     */
    public static final RewardResult EMPTY = new RewardResult(false, 0, null, null, List.of());

    /**
     * 컴팩트 생성자 — bonuses 가 null 이면 빈 리스트로 정규화한다.
     *
     * <p>외부에서 {@code new RewardResult(true, 10, "출석 기본", "ATTENDANCE_BASE", null)} 처럼
     * null 을 넘겨도 내부적으로는 {@code List.of()} 로 치환해 호출자가 안전하게 {@link #bonuses()} 를
     * 사용할 수 있게 한다. 또한 외부에서 가변 리스트를 넘기더라도 record 의 동등성/안정성을 위해
     * 그대로 보관한다(필요 시 {@code List.copyOf} 로 강화 가능하나 호출처가 모두 내부 코드라 생략).</p>
     */
    public RewardResult {
        if (bonuses == null) {
            bonuses = List.of();
        }
    }

    /**
     * 리워드 지급 성공 팩토리 메서드 (자식 보너스 없음).
     *
     * <p>기존 호출 호환성 유지용. 자식 보너스가 있으면 {@link #of(int, String, String, List)} 를
     * 사용한다.</p>
     *
     * @param points     지급된 포인트
     * @param policyName 정책 한국어 이름
     * @return 새로운 RewardResult (earned = points > 0, bonuses = 빈 리스트)
     */
    public static RewardResult of(int points, String policyName) {
        return new RewardResult(points > 0, points, policyName, null, List.of());
    }

    /**
     * 리워드 지급 성공 팩토리 메서드 (actionType + 자식 보너스 포함).
     *
     * <p>호출자가 모은 자식 보너스 결과를 {@code bonuses} 로 전달해 단일 응답으로 합산한다.
     * 자식 보너스는 호출자가 사전에 필터링(점수 0 인 EMPTY 제외)해야 한다.</p>
     *
     * @param points     본인 정책으로 지급된 포인트
     * @param policyName 정책 한국어 이름
     * @param actionType 정책의 action_type 코드 (식별용)
     * @param bonuses    자식 보너스 결과 (null 이면 자동으로 빈 리스트)
     * @return 새로운 RewardResult
     */
    public static RewardResult of(int points, String policyName, String actionType, List<RewardResult> bonuses) {
        return new RewardResult(points > 0, points, policyName, actionType, bonuses == null ? List.of() : bonuses);
    }

    /**
     * 본인 정책 + 모든 자식 보너스의 총 적립 포인트를 재귀적으로 계산한다.
     *
     * <p>{@code points} 는 본인 정책의 지급액만 담으므로, 자식 보너스를 포함한 실제 총
     * 적립 포인트가 필요할 때 본 메서드를 사용한다. 자식 보너스도 본인의 totalPoints 를
     * 사용하므로 손자 보너스까지 누적된다(현재 도메인에는 조부모-부모-자식 깊이 없지만
     * 안전한 일반화).</p>
     *
     * @return 본인 points + bonuses 각각의 totalPoints 합계
     */
    public int totalPoints() {
        int sum = points;
        for (RewardResult bonus : bonuses) {
            sum += bonus.totalPoints();
        }
        return sum;
    }

    /**
     * 동일 정책 결과에 자식 보너스를 추가한 새 RewardResult 를 반환한다.
     *
     * <p>record 는 불변이므로 새 인스턴스를 만들어 반환한다. {@code grantReward()} 가
     * 본인 정책의 amount/policyName 을 먼저 계산한 뒤 자식 보너스를 모아 합치는
     * 빌더 패턴에 사용된다.</p>
     *
     * @param newBonuses 새로 부착할 자식 보너스 (null 이면 빈 리스트)
     * @return 본인 필드는 그대로, bonuses 만 교체한 새 RewardResult
     */
    public RewardResult withBonuses(List<RewardResult> newBonuses) {
        return new RewardResult(earned, points, policyName, actionType, newBonuses == null ? List.of() : newBonuses);
    }
}
