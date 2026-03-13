---
name: api-spec
description: Spring Boot 백엔드의 전체 API 엔드포인트 목록과 DTO 스키마를 분석하여 API 명세서를 생성합니다. 프론트엔드 개발자에게 제공할 수 있는 형식으로 정리합니다.
argument-hint: "[도메인명: all|auth|user|movie|community|review|watchhistory]"
disable-model-invocation: true
allowed-tools: "Read, Grep, Glob"
---

# API 명세서 생성 스킬

## 분석 대상

`$ARGUMENTS`가 `all`이거나 지정되지 않으면 전체 도메인을, 특정 도메인이면 해당 도메인만 분석합니다.

## 분석 절차

### 1. Controller 파일 탐색
```
monglepick-backend/src/main/java/com/monglepick/monglepickbackend/domain/*/controller/*.java
```

### 2. 각 Controller에서 추출할 정보
- `@RequestMapping` — Base URL
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` — HTTP 메서드 + 경로
- `@RequestBody` — Request DTO 클래스
- `ResponseEntity<?>` — Response DTO 클래스
- `@AuthenticationPrincipal` — 인증 필요 여부
- `@PathVariable`, `@RequestParam` — 경로/쿼리 파라미터

### 3. DTO 파일 분석
```
domain/*/dto/*.java
```
- 필드명, 타입, 검증 어노테이션(@NotBlank, @Min 등)

### 4. 보안 설정 분석
```
global/config/SecurityConfig.java
```
- 공개 경로 (permitAll)
- 인증 필요 경로 (authenticated)

## 출력 형식

```markdown
## [도메인명] API

### POST /api/v1/auth/signup
- **설명**: 회원가입
- **인증**: 불필요
- **Request Body**:
  ```json
  {
    "email": "string (필수, 이메일 형식)",
    "password": "string (필수, 8자 이상)",
    "nickname": "string (필수)"
  }
  ```
- **Response 200**:
  ```json
  {
    "accessToken": "string",
    "refreshToken": "string",
    "user": {
      "id": "number",
      "email": "string",
      "nickname": "string"
    }
  }
  ```
- **Error Codes**: 409 (이메일 중복), 400 (유효성 검증 실패)
```

## 추가 정보

각 API에 대해 다음도 포함합니다:
- **관련 ERD 테이블**: 어떤 테이블을 조회/수정하는지
- **외부 서비스 의존**: AI Agent, Recommend 서비스 호출 여부
- **주의사항**: Rate Limit, 파일 크기 제한 등
