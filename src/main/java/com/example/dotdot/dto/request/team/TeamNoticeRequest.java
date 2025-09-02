package com.example.dotdot.dto.request.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "팀 공지 저장 요청")
@Getter
public class TeamNoticeRequest {
    private String notice;
}
