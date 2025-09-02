package com.example.dotdot.service;

import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Team;
import com.example.dotdot.domain.User;
import com.example.dotdot.domain.UserTeam;
import com.example.dotdot.domain.task.Task;
import com.example.dotdot.domain.task.TaskStatus;
import com.example.dotdot.dto.request.task.TaskCreateRequest;
import com.example.dotdot.dto.request.task.TaskUpdateRequest;
import com.example.dotdot.dto.response.task.TaskListResponse;
import com.example.dotdot.dto.response.task.TaskResponse;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.task.TaskErrorCode;
import com.example.dotdot.global.exception.task.TaskNotFoundException;
import com.example.dotdot.global.exception.team.ForbiddenTeamAccessException;
import com.example.dotdot.global.exception.team.TeamNotFoundException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.example.dotdot.global.exception.meeting.MeetingErrorCode.MEETING_NOT_FOUND;
import static com.example.dotdot.global.exception.task.TaskErrorCode.TASK_NOT_FOUND;
import static com.example.dotdot.global.exception.team.TeamErrorCode.FORBIDDEN_TEAM_ACCESS;
import static com.example.dotdot.global.exception.team.TeamErrorCode.TEAM_NOT_FOUND;
import static com.example.dotdot.global.exception.user.UserErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public Long createTask(Long userId, Long teamId, TaskCreateRequest request) {
        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        UserTeam assignee = userTeamRepository.findByTeam_IdAndUser_Id(team.getId(), request.getAssigneeId())
                .orElseThrow(() -> new AppException(TaskErrorCode.USER_NOT_IN_TEAM));
        checkMembershipOrThrow(user, team);

        Meeting meeting = null;
        if (request.getMeetingId() != null && request.getMeetingId() > 0) {
            meeting = meetingRepository.findById(request.getMeetingId())
                    .orElseThrow(() -> new MeetingNotFoundException(MEETING_NOT_FOUND));
            if (!meeting.getTeam().getId().equals(team.getId())) {
                throw new AppException(MeetingErrorCode.MEETING_NOT_IN_TEAM);
            }
        }

        Task saved = taskRepository.save(Task.of(
                team, meeting, assignee,
                request.getTitle(), request.getDescription(), request.getPriority(),
                request.getStatus(), request.getDue() // DTO가 LocalDate를 제공한다고 가정
        ));
        return saved.getId();
    }

    public TaskResponse getTask(Long userId, Long taskId) {
        User user = getUserOrThrow(userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
        Team team = getTeamOrThrow(task.getTeam().getId());
        checkMembershipOrThrow(user, team);
        return TaskResponse.from(task);
    }

    @Transactional
    public void updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        User user = getUserOrThrow(userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
        Team team = getTeamOrThrow(task.getTeam().getId());
        checkMembershipOrThrow(user, team);

        UserTeam newAssignee = null;
        if (request.getAssigneeId() != null) {
            newAssignee = userTeamRepository.findByTeam_IdAndUser_Id(task.getTeam().getId(), request.getAssigneeId())
                    .orElseThrow(() -> new AppException(TaskErrorCode.USER_NOT_IN_TEAM));
        }

        task.update(
                request.getTitle(), request.getDescription(), newAssignee,
                request.getPriority(), request.getStatus(), request.getDue() // DTO가 LocalDate를 제공한다고 가정
        );
    }

    @Transactional
    public void changeStatus(Long userId, Long taskId, TaskStatus status) {
        User user = getUserOrThrow(userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(TASK_NOT_FOUND));
        Team team = getTeamOrThrow(task.getTeam().getId());
        checkMembershipOrThrow(user, team);
        task.changeStatus(status != null ? status : TaskStatus.TODO);
    }

    // ⭐ [수정] 메소드 전체를 아래 코드로 교체합니다.
    public TaskListResponse listTasks(
            Long userId,
            Long teamId,
            LocalDate startDate,
            LocalDate endDate,
            Long meetingIdOrNull,
            Long assigneeUserIdOrNull,
            Pageable pageable) {

        System.out.println("월별 조회 API 호출됨! 시작일: " + startDate + ", 종료일: " + endDate);

        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        // [수정] 날짜 파라미터가 null일 경우를 대비해 기본값을 설정합니다.
        LocalDate searchStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate searchEndDate = (endDate != null) ? endDate : searchStartDate;

        // [수정] Repository에 LocalDateTime 대신 LocalDate를 그대로 전달합니다.
        Page<Task> page = taskRepository.searchTeamTasks(
                teamId, searchStartDate, searchEndDate, meetingIdOrNull, assigneeUserIdOrNull, pageable
        );

        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        taskRepository.countByStatusForTeam(teamId, searchStartDate, searchEndDate, meetingIdOrNull, assigneeUserIdOrNull)
                .forEach(sc -> counts.put(sc.getStatus(), sc.getCount()));

        long todo = counts.getOrDefault(TaskStatus.TODO, 0L);
        long processing = counts.getOrDefault(TaskStatus.PROCESSING, 0L);
        long done = counts.getOrDefault(TaskStatus.DONE, 0L);

        List<TaskResponse> items = page.getContent().stream()
                .map(TaskResponse::from)
                .toList();

        TaskListResponse.Summary summary = TaskListResponse.Summary.builder()
                .todo(todo).processing(processing).done(done)
                .build();

        TaskListResponse.PageMeta pageMeta = TaskListResponse.PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return TaskListResponse.builder()
                .items(items)
                .summary(summary)
                .page(pageMeta)
                .build();
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        User user = getUserOrThrow(userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
        checkMembershipOrThrow(user, task.getTeam());
        taskRepository.delete(task);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }

    private Team getTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND));
    }

    private void checkMembershipOrThrow(User user, Team team) {
        if (!userTeamRepository.existsByUserAndTeam(user, team)) {
            throw new ForbiddenTeamAccessException(FORBIDDEN_TEAM_ACCESS);
        }
    }
}