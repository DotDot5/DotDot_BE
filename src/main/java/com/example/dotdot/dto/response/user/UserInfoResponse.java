package com.example.dotdot.dto.response.user;

import com.example.dotdot.domain.User;
import lombok.Builder;

public record UserInfoResponse(
        String name,
        String email,
        String profileImageUrl,
        String position,
        String department
) {

    @Builder
    public UserInfoResponse {

    }

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .position(user.getPosition())
                .department(user.getDepartment())
                .build();
    }
}