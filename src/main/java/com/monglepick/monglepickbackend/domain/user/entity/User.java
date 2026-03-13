package com.monglepick.monglepickbackend.domain.user.entity;

import com.monglepick.monglepickbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티 — users 테이블 매핑.
 *
 * <p>몽글픽 서비스의 사용자 정보를 저장한다.
 * 로컬 회원가입과 소셜 로그인(네이버/카카오/구글)을 모두 지원한다.</p>
 *
 * <h3>기본 프로필 필드</h3>
 * <ul>
 *   <li>{@code userId} — 사용자 고유 ID (VARCHAR(50) PK, AUTO_INCREMENT 아님)</li>
 *   <li>{@code nickname} — 닉네임 (표시명)</li>
 *   <li>{@code email} — 이메일 주소</li>
 *   <li>{@code profileImage} — 프로필 이미지 URL</li>
 *   <li>{@code ageGroup} — 연령대 (예: "20s", "30s")</li>
 *   <li>{@code gender} — 성별 (예: "M", "F", "OTHER")</li>
 * </ul>
 *
 * <h3>인증 관련 필드</h3>
 * <ul>
 *   <li>{@code passwordHash} — BCrypt 해시 비밀번호 (소셜 로그인 시 null)</li>
 *   <li>{@code provider} — 로그인 제공자 (LOCAL, NAVER, KAKAO, GOOGLE)</li>
 *   <li>{@code providerId} — 소셜 제공자 고유 ID (소셜 로그인 시 사용)</li>
 *   <li>{@code userRole} — 사용자 역할 (USER, ADMIN)</li>
 * </ul>
 *
 * <h3>추가 개인정보 필드</h3>
 * <ul>
 *   <li>{@code userBirth} — 생년월일 (YYYYMMDD 형식)</li>
 *   <li>{@code optionTerm} — 선택 약관 동의 여부</li>
 *   <li>{@code requiredTerm} — 필수 약관 동의 여부</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    /**
     * 로그인 제공자 열거형.
     * LOCAL: 자체 회원가입 (이메일+비밀번호),
     * NAVER/KAKAO/GOOGLE: 소셜 로그인 (OAuth2)
     */
    public enum Provider {
        LOCAL, NAVER, KAKAO, GOOGLE
    }

    /**
     * 사용자 고유 ID (PK).
     * VARCHAR(50) 문자열 PK이며, AUTO_INCREMENT가 아니다.
     * JWT 토큰의 subject(sub)로 사용된다.
     */
    @Id
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    /** 사용자 닉네임 (표시명, 최대 100자) */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /** 이메일 주소 (최대 200자) */
    @Column(name = "email", length = 200)
    private String email;

    /** 프로필 이미지 URL (최대 500자) */
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    /**
     * 연령대 (최대 10자).
     * 예: "10s", "20s", "30s", "40s", "50s"
     */
    @Column(name = "age_group", length = 10)
    private String ageGroup;

    /**
     * 성별 (최대 10자).
     * 예: "M"(남성), "F"(여성), "OTHER"(기타)
     */
    @Column(name = "gender", length = 10)
    private String gender;

    /**
     * BCrypt 해시 비밀번호 (최대 255자).
     * 로컬(LOCAL) 회원가입 시에만 설정되며,
     * 소셜 로그인 사용자는 null이다.
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /**
     * 로그인 제공자.
     * 기본값: LOCAL (자체 회원가입).
     * 소셜 로그인 시 NAVER, KAKAO, GOOGLE 중 하나가 설정된다.
     * MySQL ENUM('LOCAL','NAVER','KAKAO','GOOGLE') 타입으로 저장된다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 10)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    /**
     * 소셜 제공자 고유 ID (최대 200자).
     * 소셜 로그인 시 제공자(네이버/카카오/구글)가 발급하는 사용자 고유 식별자.
     * 로컬 회원가입 시에는 null이다.
     */
    @Column(name = "provider_id", length = 200)
    private String providerId;

    /**
     * 사용자 역할 (최대 20자).
     * 기본값: "USER".
     * 관리자: "ADMIN".
     */
    @Column(name = "user_role", length = 20)
    @Builder.Default
    private String userRole = "USER";

    /**
     * 생년월일 (최대 20자).
     * YYYYMMDD 형식 문자열 (예: "19950101").
     */
    @Column(name = "user_birth", length = 20)
    private String userBirth;

    /**
     * 선택 약관 동의 여부.
     * 기본값: false.
     * 마케팅 수신 동의 등 선택 사항.
     */
    @Column(name = "option_term")
    @Builder.Default
    private Boolean optionTerm = false;

    /**
     * 필수 약관 동의 여부.
     * 기본값: false.
     * 서비스 이용약관, 개인정보처리방침 등 필수 사항.
     */
    @Column(name = "required_term")
    @Builder.Default
    private Boolean requiredTerm = false;
}
