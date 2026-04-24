package com.monglepick.monglepickbackend.domain.support.repository;

import com.monglepick.monglepickbackend.domain.support.entity.SupportFaqFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * FAQ 피드백 JPA 레포지토리.
 *
 * <p>{@link SupportFaqFeedback} 엔티티에 대한 데이터 접근 계층.
 * 중복 피드백 제출 방지를 위한 사전 조회 메서드를 제공한다.</p>
 */
public interface SupportFaqFeedbackRepository extends JpaRepository<SupportFaqFeedback, Long> {

    /**
     * 특정 FAQ에 대한 특정 사용자의 피드백을 조회한다.
     *
     * <p>피드백 제출 전 중복 여부를 확인하는 데 사용한다.
     * (faq_id, user_id) 복합 유니크 제약이 DB에 걸려 있으므로
     * 서비스 레이어에서 이 메서드로 사전 검사 후 중복이면
     * {@code ErrorCode.FAQ_FEEDBACK_DUPLICATE} 예외를 던진다.</p>
     *
     * <p>Spring Data JPA 메서드 이름 규칙:
     * {@code faq.faqId} 경로 → {@code findByFaq_FaqId},
     * userId 필드 → {@code AndUserId}</p>
     *
     * <pre>{@code
     * // 사용 예 (서비스 레이어)
     * feedbackRepository.findByFaq_FaqIdAndUserId(faqId, userId)
     *     .ifPresent(f -> { throw new BusinessException(ErrorCode.FAQ_FEEDBACK_DUPLICATE); });
     * }</pre>
     *
     * @param faqId  피드백 대상 FAQ ID
     * @param userId 피드백 제출 사용자 ID
     * @return 기존 피드백이 존재하면 Optional.of(feedback), 없으면 Optional.empty()
     */
    Optional<SupportFaqFeedback> findByFaq_FaqIdAndUserId(Long faqId, String userId);

    /**
     * 특정 사용자의 여러 FAQ 피드백을 일괄 조회한다.
     *
     * <p>GET /api/v1/support/faq 응답에 사용자의 기존 피드백 상태를 포함하기 위한 batch 조회.
     * FAQ 목록을 먼저 조회한 뒤 해당 id 집합과 userId 로 한 번에 조회하여 N+1 을 피한다.</p>
     *
     * @param userId 조회 대상 사용자 ID (비어있지 않아야 함)
     * @param faqIds FAQ id 집합 (비어있으면 빈 리스트 반환)
     * @return 사용자 피드백 목록 (없으면 빈 리스트)
     */
    List<SupportFaqFeedback> findByUserIdAndFaq_FaqIdIn(String userId, Collection<Long> faqIds);
}
