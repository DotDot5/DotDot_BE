package com.example.dotdot.dto.request.task;

import com.example.dotdot.domain.task.TaskPriority;
import com.example.dotdot.domain.task.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long assigneeId;

    @Schema(defaultValue = "MEDIUM")
    private TaskPriority priority;

    @Schema(defaultValue = "TODO")
    private TaskStatus status;

    @NotNull
    private LocalDate due;

    @Schema(description = "연결할 회의 ID, 회의 없이 생성 시 null")
    private Long meetingId; // 선택 필드
}
