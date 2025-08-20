package com.example.dotdot.dto.response.task;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDraft {
    String assigneeName;
    String title;
    String description;
    String due;
    String priority;
}
