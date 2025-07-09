package com.example.dotdot.dto.response.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "팀원 목록 조회")
@Getter
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private Long userId;
    private String name;
    private String profileImageUrl;
    private String role;
}
