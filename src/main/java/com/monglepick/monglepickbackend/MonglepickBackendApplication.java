package com.monglepick.monglepickbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 몽글픽 백엔드 애플리케이션 진입점.
 *
 * <p>Spring Boot 4.0.3 기반의 영화 추천 서비스 백엔드 서버.
 * JPA + MySQL 연동, JWT 인증, REST API 제공.</p>
 */
@SpringBootApplication
public class MonglepickBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonglepickBackendApplication.class, args);
    }
}
