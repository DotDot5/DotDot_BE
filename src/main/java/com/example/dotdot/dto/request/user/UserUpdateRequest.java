package com.example.dotdot.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    @NotBlank(message = "직책은 필수입니다")
    private String position;
}
