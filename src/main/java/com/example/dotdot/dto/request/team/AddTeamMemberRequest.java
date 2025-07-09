package com.example.dotdot.dto.request.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Schema(description = "팀원 초대 요청")
@Getter
public class AddTeamMemberRequest {
    @NotBlank
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;
}
