package com.example.dotdot.dto.response.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "특정 팀 상세 조회")
@Getter
@Builder
@AllArgsConstructor
public class TeamDetailResponse {
    private Long teamId;
    private String teamName;
    private String notice;
    private List<TeamMemberResponse> members;
}
