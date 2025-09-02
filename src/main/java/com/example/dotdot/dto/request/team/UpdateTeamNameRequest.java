package com.example.dotdot.dto.request.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "팀 이름 수정 요청")
public class UpdateTeamNameRequest {
    @Schema(description = "변경할 팀 이름", example = "새로운 팀 이름")
    @NotBlank(message = "팀 이름은 필수입니다.")
    private String teamName;
}
