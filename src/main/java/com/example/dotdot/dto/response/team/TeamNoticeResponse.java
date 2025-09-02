package com.example.dotdot.dto.response.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "팀 공지 조회")
@Getter
@AllArgsConstructor
public class TeamNoticeResponse {
    private String notice;
}
