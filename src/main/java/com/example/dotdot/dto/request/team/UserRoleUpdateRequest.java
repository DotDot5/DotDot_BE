package com.example.dotdot.dto.request.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "팀원 역할 수정 요청")
@Getter
public class UserRoleUpdateRequest {
    private String role;
}
