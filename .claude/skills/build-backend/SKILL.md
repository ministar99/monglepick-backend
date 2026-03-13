---
name: build-backend
description: Spring Boot 백엔드 프로젝트를 빌드하고 결과를 분석합니다. Gradle 빌드, 컴파일 체크, 테스트 실행, JAR 생성을 수행합니다.
argument-hint: "[build|compile|test|jar|clean]"
disable-model-invocation: true
allowed-tools: "Bash, Read, Grep"
---

# Spring Boot 빌드 스킬

## 환경

- **프로젝트 경로**: `/Users/yoonhyungjoo/Documents/monglepick/monglepick-backend`
- **Java**: 21 (Eclipse Temurin)
- **Gradle**: 8.12
- **Spring Boot**: 4.0.3

## 빌드 모드

`$ARGUMENTS`에 따라 실행 모드를 결정합니다:

### 전체 빌드 (`build` 또는 인자 없음)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew build 2>&1
```

### 컴파일만 (`compile`)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew compileJava 2>&1
```

### 테스트만 (`test`)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew test --info 2>&1
```

### JAR 생성 (`jar`)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew bootJar -x test 2>&1
```

### 클린 빌드 (`clean`)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew clean build 2>&1
```

## 결과 분석

### 빌드 성공 시
- BUILD SUCCESSFUL 메시지 확인
- 생성된 JAR 파일 경로 및 크기 표시
- 경고(warning) 사항 정리

### 빌드 실패 시
1. **컴파일 에러**: 에러 파일:라인 + 원인 분석
2. **테스트 실패**: 실패한 테스트 목록 + 에러 메시지
3. **의존성 문제**: 누락된 라이브러리 + 해결 방법 제안
4. 관련 소스 코드를 읽고 수정 방안을 제시합니다

## 의존성 참조

| 라이브러리 | 용도 |
|-----------|------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa | JPA + Hibernate |
| spring-boot-starter-security | Spring Security |
| spring-boot-starter-validation | Bean Validation |
| mysql-connector-j | MySQL JDBC |
| jjwt 0.12.6 | JWT 인증 |
| lombok | 보일러플레이트 제거 |
