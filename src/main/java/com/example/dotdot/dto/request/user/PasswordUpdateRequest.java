package com.example.dotdot.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateRequest {
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";

    @NotBlank(message = "토큰은 필수입니다")
    private String token;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
            regexp = PASSWORD_REGEX,
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8-20자리여야 합니다"
    )
    private String newPassword;
}