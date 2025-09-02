package com.example.dotdot.domain.task;

import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Participant;
import com.example.dotdot.domain.Team;
import com.example.dotdot.domain.UserTeam;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "meeting_id", nullable = true)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignee_id", nullable = false)
    private UserTeam assignee;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(nullable = false)
    private LocalDate due;

    @Formula("case priority when 'HIGH' then 1 when 'MEDIUM' then 2 when 'LOW' then 3 else 99 end")
    private int priorityOrder;

    @Formula("case status when 'TODO' then 1 when 'PROCESSING' then 2 when 'DONE' then 3 else 99 end")
    private int statusOrder;

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    public void update(String title,
                       String description,
                       UserTeam assignee,
                       TaskPriority priority,
                       TaskStatus status,
                       LocalDate due) {
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
        if (assignee != null) this.assignee = assignee;
        if (priority != null) this.priority = priority;
        if (status != null) this.status = status;
        if (due != null) this.due = due;
    }
    public static Task of(Team team,
                          Meeting meeting,
                          UserTeam assignee,
                          String title,
                          String description,
                          TaskPriority priority,
                          TaskStatus status,
                          LocalDate due) {
        return Task.builder()
                .team(team)
                .meeting(meeting)
                .assignee(assignee)
                .title(title)
                .description(description)
                .priority(priority != null ? priority : TaskPriority.MEDIUM) // 기본 보통
                .status(status != null ? status : TaskStatus.TODO)           // 기본 대기
                .due(due)
                .build();
    }

}
