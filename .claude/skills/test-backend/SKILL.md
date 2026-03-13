---
name: test-backend
description: Spring Boot 백엔드의 테스트를 실행합니다. JUnit 5 기반 단위/통합 테스트를 수행하고 결과를 분석합니다.
argument-hint: "[all|클래스명]"
disable-model-invocation: true
allowed-tools: "Bash, Read, Grep"
---

# Spring Boot 테스트 실행 스킬

## 환경

- **프로젝트 경로**: `/Users/yoonhyungjoo/Documents/monglepick/monglepick-backend`
- **테스트 프레임워크**: JUnit 5 + Spring Boot Test + Spring Security Test
- **DB 의존성**: MySQL 필요 (또는 H2/Testcontainers로 대체 가능)

## 실행 모드

### 전체 테스트 (`all` 또는 인자 없음)
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew test --info 2>&1
```

### 특정 테스트 클래스
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew test --tests "*$ARGUMENTS*" --info 2>&1
```

### 특정 테스트 메서드
```bash
cd /Users/yoonhyungjoo/Documents/monglepick/monglepick-backend && ./gradlew test --tests "*$ARGUMENTS" --info 2>&1
```

## 결과 분석

### 성공 시
- 전체 테스트 수, 통과 수, 실행 시간 요약
- 경고 사항 정리

### 실패 시
1. 실패한 테스트 목록
2. 에러 메시지 + 스택트레이스 분석
3. 관련 소스 코드 읽고 원인 파악
4. 수정 방안 제시

### 테스트 리포트 위치
```
build/reports/tests/test/index.html
```

## 테스트 작성 패턴

### Controller 테스트 (@WebMvcTest)
```java
@WebMvcTest(SomeController.class)
class SomeControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean SomeService someService;

    @Test
    void 정상_요청() throws Exception {
        mockMvc.perform(get("/api/v1/some")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
```

### Service 테스트
```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {
    @Mock SomeRepository repository;
    @InjectMocks SomeService service;
}
```

### Repository 테스트 (@DataJpaTest)
```java
@DataJpaTest
class SomeRepositoryTest {
    @Autowired SomeRepository repository;
    @Autowired TestEntityManager entityManager;
}
```
