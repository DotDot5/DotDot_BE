package com.example.dotdot.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordChangeRequest {

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";

    @NotBlank(message = "현재 비밀번호를 입력하세요")
    @Pattern(
            regexp = PASSWORD_REGEX,
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8-20자리여야 합니다"
    )
    String currentPassword;

    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    @Pattern(
            regexp = PASSWORD_REGEX,
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8-20자리여야 합니다"
    )
    String newPassword;
}
