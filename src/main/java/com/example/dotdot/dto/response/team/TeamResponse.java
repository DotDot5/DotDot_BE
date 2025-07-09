package com.example.dotdot.dto.response.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "내가 속한 팀 목록 조회")
@Getter
@AllArgsConstructor
public class TeamResponse {
    private Long teamId;
    private String teamName;
}
