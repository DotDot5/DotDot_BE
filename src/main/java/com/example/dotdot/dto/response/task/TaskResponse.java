package com.example.dotdot.dto.response.task;

import com.example.dotdot.domain.task.Task;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long taskId;
    private Long teamId;
    private Long meetingId;

    private String title;
    private String description;

    private Long assigneeUserId;
    private String assigneeName;
    private String assigneeProfileImageUrl;

    private String priorityLabel;

    private String statusLabel;

    private LocalDateTime due;

    public static TaskResponse from(Task t) {
        Long meetingId = (t.getMeeting() != null) ? t.getMeeting().getId() : null;

        var assigneeUser = (t.getAssignee() != null) ? t.getAssignee().getUser() : null;
        Long assigneeUserId = (assigneeUser != null) ? assigneeUser.getId() : null;
        String assigneeName = (assigneeUser != null) ? assigneeUser.getName() : null;
        String assigneeProfile = (assigneeUser != null) ? assigneeUser.getProfileImageUrl() : null;

        String priorityLabel = t.getPriority().getKo();
        String statusLabel = t.getStatus().getKo();

        return TaskResponse.builder()
                .taskId(t.getId())
                .teamId(t.getTeam().getId())
                .meetingId(meetingId)
                .title(t.getTitle())
                .description(t.getDescription())
                .assigneeUserId(assigneeUserId)
                .assigneeName(assigneeName)
                .assigneeProfileImageUrl(assigneeProfile)
                .priorityLabel(priorityLabel)
                .statusLabel(statusLabel)
                .due(t.getDue())
                .build();
    }

}
