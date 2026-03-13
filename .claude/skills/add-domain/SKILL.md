---
name: add-domain
description: Spring Boot 백엔드에 새로운 도메인을 스캐폴딩합니다. DDD 패턴에 맞게 controller, dto, entity, repository, service 패키지와 기본 파일을 생성합니다.
argument-hint: "[도메인명 (영문 소문자)]"
allowed-tools: "Read, Edit, Write, Glob, Grep, Bash"
---

# 도메인 스캐폴딩 스킬

`$ARGUMENTS` 이름의 새 도메인을 DDD 패턴으로 생성합니다.

## 기존 도메인 구조 참조

```
src/main/java/com/monglepick/monglepickbackend/domain/
├── auth/          ✅ 구현됨 (회원가입/로그인/토큰갱신)
├── user/          ✅ 구현됨 (프로필/선호도)
├── movie/         ✅ 구현됨 (영화 조회/상세)
├── community/     ✅ 구현됨 (게시글 CRUD)
├── review/        ✅ 구현됨 (리뷰 CRUD)
├── watchhistory/  ✅ 구현됨 (시청이력/위시리스트)
├── playlist/      📁 빈 디렉토리
├── quiz/          📁 빈 디렉토리
├── reward/        📁 빈 디렉토리
├── support/       📁 빈 디렉토리
└── admin/         📁 빈 디렉토리
```

## Step 1: 패키지 디렉토리 생성

```
domain/$ARGUMENTS/
├── controller/
│   └── ${대문자}Controller.java
├── dto/
│   ├── ${대문자}Request.java
│   └── ${대문자}Response.java
├── entity/
│   └── ${대문자}.java
├── repository/
│   └── ${대문자}Repository.java
└── service/
    └── ${대문자}Service.java
```

## Step 2: Entity 생성

```java
package com.monglepick.monglepickbackend.domain.$ARGUMENTS.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * $ARGUMENTS 엔티티.
 *
 * [엔티티 설명]
 */
@Entity
@Table(name = "${테이블명}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ${대문자} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 도메인 필드 추가

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public ${대문자}(/* 생성자 파라미터 */) {
        // TODO: 필드 초기화
    }
}
```

## Step 3: Repository 생성

```java
package com.monglepick.monglepickbackend.domain.$ARGUMENTS.repository;

import com.monglepick.monglepickbackend.domain.$ARGUMENTS.entity.${대문자};
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * $ARGUMENTS 리포지토리.
 */
public interface ${대문자}Repository extends JpaRepository<${대문자}, Long> {
    // TODO: 커스텀 쿼리 메서드 추가
}
```

## Step 4: DTO 생성

```java
// Request
package com.monglepick.monglepickbackend.domain.$ARGUMENTS.dto;

import jakarta.validation.constraints.NotBlank;

public record ${대문자}Request(
    @NotBlank(message = "필수 항목입니다")
    String fieldName
) {}

// Response
public record ${대문자}Response(
    Long id,
    String fieldName,
    LocalDateTime createdAt
) {
    public static ${대문자}Response from(${대문자} entity) {
        return new ${대문자}Response(
            entity.getId(),
            entity.getFieldName(),
            entity.getCreatedAt()
        );
    }
}
```

## Step 5: Service 생성

```java
package com.monglepick.monglepickbackend.domain.$ARGUMENTS.service;

import com.monglepick.monglepickbackend.domain.$ARGUMENTS.repository.${대문자}Repository;
import com.monglepick.monglepickbackend.global.exception.BusinessException;
import com.monglepick.monglepickbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * $ARGUMENTS 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ${대문자}Service {

    private final ${대문자}Repository repository;

    // TODO: 비즈니스 로직 구현
}
```

## Step 6: Controller 생성

```java
package com.monglepick.monglepickbackend.domain.$ARGUMENTS.controller;

import com.monglepick.monglepickbackend.domain.$ARGUMENTS.service.${대문자}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * $ARGUMENTS API 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/${URL경로}")
@RequiredArgsConstructor
public class ${대문자}Controller {

    private final ${대문자}Service service;

    // TODO: API 엔드포인트 구현
}
```

## Step 7: 빌드 확인

```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew compileJava 2>&1
```

## 패턴 규칙

1. **Lombok 사용**: @Getter, @Builder, @RequiredArgsConstructor, @NoArgsConstructor
2. **record DTO**: Java record 사용 (불변, 간결)
3. **@Valid 검증**: Controller에서 @Valid + @RequestBody
4. **예외 처리**: BusinessException + ErrorCode enum 사용
5. **트랜잭션**: Service 클래스에 @Transactional(readOnly = true), 변경 메서드에 @Transactional
6. **인증**: 보호 엔드포인트에 @AuthenticationPrincipal Long userId
