package com.example.dotdot.dto.request.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Schema(description = "팀 이름으로 추가")
@Getter
public class CreateTeamRequest {
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Schema(description = "생성할 팀 이름", example = "캡스톤 팀A")
    private String teamName;
}
