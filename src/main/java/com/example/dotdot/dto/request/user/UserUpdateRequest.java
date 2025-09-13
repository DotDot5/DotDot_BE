package com.example.dotdot.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "직책은 필수입니다")
    private String position;

    @Schema(description = "부서", example = "개발팀")
    private String department;

    @Schema(description = "프로필 이미지 URL", example = "https://...")
    private String profileImageUrl;
}