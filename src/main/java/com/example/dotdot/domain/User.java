package com.example.dotdot.domain;

import com.example.dotdot.dto.request.user.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private String email;

    private String password;

    private String profileImageUrl;

    private String position; // ex) 슝슝회사 대리

//    private String audioUrl; // 사용자별 음성 링크

    private String refreshToken;

    // refreshToken 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 사용자 정보 업데이트
    public void updateUserInfo(UserUpdateRequest request) {
        this.name = request.getName();
        this.email = request.getEmail();
        this.position = request.getPosition();
    }

    // 프로필 이미지 업데이트
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // 비밀번호 업데이트
    public void updatePassword(String password) {
        this.password = password;
    }
}
