package com.example.dotdot.dto.request.task;

import com.example.dotdot.domain.task.TaskPriority;
import com.example.dotdot.domain.task.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUpdateRequest {
    private String title;
    private String description;
    private Long assigneeId;     // 변경 시에만 세팅
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDateTime due;
}
