package com.example.dotdot.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    @Email
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
}