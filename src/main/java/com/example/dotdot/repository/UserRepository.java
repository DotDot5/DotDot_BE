package com.example.dotdot.repository;

import com.example.dotdot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    //  이메일로 사용자를 찾는 메소드 (로그인, 인증 등에 사용)
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    //이메일이 존재하는지 확인하는 메소드 (회원가입 시 중복 체크에 사용)
    boolean existsByEmailAndDeletedAtIsNull(String email);

    //ID로 사용자를 찾는 메소드 (일반적인 사용자 정보 조회에 사용)
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}

