package com.monglepick.monglepickbackend.domain.user.repository;

import com.monglepick.monglepickbackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 리포지토리.
 *
 * <p>users 테이블에 대한 기본 CRUD를 지원한다.
 * PK는 user_id (VARCHAR(50))이므로 JpaRepository의 ID 타입은 String이다.</p>
 */
public interface UserRepository extends JpaRepository<User, String> {
}
