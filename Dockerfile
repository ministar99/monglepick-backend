# =========================================
# 몽글픽 Spring Boot 백엔드 Docker 이미지
# =========================================
# 멀티스테이지 빌드: Gradle 빌드 → JRE 런타임 이미지
# Java 21 기반, 비루트 사용자로 실행

# --- 1단계: Gradle 빌드 ---
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Gradle 설정 파일 복사 (캐시 레이어 활용)
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# 의존성 미리 다운로드 (소스 변경 시 캐시 재사용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src/ src/

# JAR 빌드 (테스트 제외)
RUN gradle bootJar --no-daemon -x test

# --- 2단계: 런타임 이미지 ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# curl 설치 (헬스체크용)
RUN apk add --no-cache curl

# JAR 복사 (빌드 결과물)
COPY --from=builder /app/build/libs/*.jar app.jar

# 비루트 사용자 생성 (보안)
RUN adduser -D -s /bin/sh appuser
USER appuser

# 헬스체크용 포트 노출
EXPOSE 8080

# Spring Boot 실행
# JVM 메모리 설정은 환경변수 JAVA_OPTS로 주입 가능
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--Xms512m -Xmx1536m} -jar app.jar"]
