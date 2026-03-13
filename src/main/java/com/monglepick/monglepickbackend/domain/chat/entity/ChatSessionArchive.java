package com.monglepick.monglepickbackend.domain.chat.entity;

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
 * 채팅 세션 아카이브 엔티티 — chat_session_archive 테이블 매핑.
 *
 * <p>AI Agent와의 채팅 세션이 종료된 후, Redis에서 MySQL로 아카이빙된 대화 기록을 저장한다.
 * 실시간 대화는 Redis에서 관리되며, 세션 종료/만료 시 이 테이블로 영속화된다.</p>
 *
 * <h3>주요 필드</h3>
 * <ul>
 *   <li>{@code user} — 대화 참여 사용자 (FK → users.user_id)</li>
 *   <li>{@code sessionId} — 세션 UUID (UNIQUE, Redis 세션 키와 동일)</li>
 *   <li>{@code messages} — 전체 대화 내역 (JSON 배열, 필수)</li>
 *   <li>{@code turnCount} — 대화 턴 수 (사용자 메시지 수)</li>
 *   <li>{@code intentSummary} — 세션 중 감지된 의도 요약 (JSON)</li>
 *   <li>{@code startedAt} — 세션 시작 시각</li>
 *   <li>{@code endedAt} — 세션 종료 시각 (아직 진행 중이면 NULL)</li>
 * </ul>
 *
 * <h3>messages JSON 구조</h3>
 * <pre>
 * [
 *   {"role": "user", "content": "우울한데 영화 추천해줘", "timestamp": "..."},
 *   {"role": "assistant", "content": "...", "timestamp": "...", "movies": [...]}
 * ]
 * </pre>
 */
@Entity
@Table(name = "chat_session_archive")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatSessionArchive {

    /** 아카이브 고유 ID (BIGINT AUTO_INCREMENT PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대화 참여 사용자.
     * chat_session_archive.user_id → users.user_id FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 세션 UUID (v4).
     * Redis 세션 키와 동일한 값이며, UNIQUE 제약이 있다.
     */
    @Column(name = "session_id", length = 36, nullable = false, unique = true)
    private String sessionId;

    /**
     * 전체 대화 내역 (JSON 배열, 필수).
     * user/assistant 메시지를 시간순으로 저장한다.
     */
    @Column(name = "messages", columnDefinition = "json", nullable = false)
    private String messages;

    /** 대화 턴 수 (사용자 메시지 수, 기본값: 0) */
    @Column(name = "turn_count")
    @Builder.Default
    private Integer turnCount = 0;

    /**
     * 세션 중 감지된 의도 요약 (JSON 객체).
     * 예: {"recommend": 3, "search": 1, "general": 2}
     */
    @Column(name = "intent_summary", columnDefinition = "json")
    private String intentSummary;

    /** 세션 시작 시각 */
    @CreationTimestamp
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 세션 종료 시각.
     * 세션이 아직 진행 중이면 NULL이다.
     */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** 레코드 생성 시각 (아카이빙 시각) */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
