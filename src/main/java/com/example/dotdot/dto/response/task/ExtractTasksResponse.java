package com.example.dotdot.dto.response.task;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractTasksResponse {
    Long meetingId;
    int created;
    int skipped;
    java.util.List<TaskDraft> drafts;
}
