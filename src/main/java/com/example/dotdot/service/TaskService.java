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
import com.example.dotdot.global.exception.task.AssigneeNotInTeamException;
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
import java.time.LocalDateTime;
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

    //task 생성
    @Transactional
    public Long createTask(Long userId, Long teamId, TaskCreateRequest request) {
        User user =getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);

        UserTeam assignee = userTeamRepository.findByTeam_IdAndUser_Id(team.getId(), request.getAssigneeId())
                .orElseThrow(() -> new AppException(TaskErrorCode.USER_NOT_IN_TEAM));

        checkMembershipOrThrow(user,team);

        Long meetingId = request.getMeetingId();
        if (meetingId != null && meetingId <= 0) {
            meetingId = null; // 0, 음수는 회의 없음으로 간주
        }

        Meeting meeting = null;
        if(meetingId!=null){
            meeting = meetingRepository.findById(request.getMeetingId())
                    .orElseThrow(() -> new MeetingNotFoundException(MEETING_NOT_FOUND));
            if (!meeting.getTeam().getId().equals(team.getId())) {
                throw new AppException(MeetingErrorCode.MEETING_NOT_IN_TEAM); // 없으면 FORBIDDEN으로 대체
            }
        }

        Task saved=taskRepository.save(Task.of(
                team,
                meeting,
                assignee,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getStatus(),
                request.getDue()
                ));
        return saved.getId();
    }

    //task 조회
    public TaskResponse getTask(Long userId, Long taskId) {
        User user = getUserOrThrow(userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

        Team team = getTeamOrThrow(task.getTeam().getId());

        checkMembershipOrThrow(user,team);
        return TaskResponse.from(task);
    }

    //task 수정
    @Transactional
    public void updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        User user = getUserOrThrow(userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
        Team team = getTeamOrThrow(task.getTeam().getId());

        checkMembershipOrThrow(user,team);

        UserTeam newAssignee = null;
        if (request.getAssigneeId() != null) {
            // 담당자 변경 시에도 같은 팀 소속인지 점검
            newAssignee = userTeamRepository.findByTeam_IdAndUser_Id(task.getTeam().getId(), request.getAssigneeId())
                    .orElseThrow(() -> new AppException(TaskErrorCode.USER_NOT_IN_TEAM));
        }

        task.update(
                request.getTitle(),
                request.getDescription(),
                newAssignee,
                request.getPriority(),
                request.getStatus(),
                request.getDue()
        );
    }

    //task 상태 변경
    @Transactional
    public void changeStatus(Long userId, Long taskId, TaskStatus status) {
        User user = getUserOrThrow(userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(TaskErrorCode.TASK_NOT_FOUND));

        Team team = getTeamOrThrow(task.getTeam().getId());

        checkMembershipOrThrow(user,team);

        task.changeStatus(status != null ? status : TaskStatus.TODO);

    }

    //task 목록 및 요약 반환
    public TaskListResponse listTasks(
            Long userId,
            Long teamId,
            LocalDate date,                 // 달력 선택 날짜 (null이면 오늘)
            Long meetingIdOrNull,           // 회의 필터 (null이면 전체)
            Long assigneeUserIdOrNull,      // 전체팀원=null, 특정 팀원=userId
            Pageable pageable) {

        User user =getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        LocalDate target=(date!=null)?date:LocalDate.now();
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end = target.plusDays(1).atStartOfDay();

        Page<Task> page = taskRepository.searchTeamTasks(
                teamId, start, end, meetingIdOrNull, assigneeUserIdOrNull, pageable
        );

        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        taskRepository.countByStatusForTeam(teamId, start, end, meetingIdOrNull, assigneeUserIdOrNull)
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


