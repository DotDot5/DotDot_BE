package com.example.dotdot.service;

import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Team;
import com.example.dotdot.domain.User;
import com.example.dotdot.domain.UserTeam;
import com.example.dotdot.domain.task.Task;
import com.example.dotdot.domain.task.TaskPriority;
import com.example.dotdot.domain.task.TaskStatus;
import com.example.dotdot.dto.request.task.ExtractTasksRequest;
import com.example.dotdot.dto.request.task.TaskCreateRequest;
import com.example.dotdot.dto.request.task.TaskUpdateRequest;
import com.example.dotdot.dto.response.task.ExtractTasksResponse;
import com.example.dotdot.dto.response.task.TaskDraft;
import com.example.dotdot.dto.response.task.TaskListResponse;
import com.example.dotdot.dto.response.task.TaskResponse;
import com.example.dotdot.global.client.OpenAITaskExtractClient;
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
import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final OpenAITaskExtractClient taskExtractClient;
    private final AgendaRepository agendaRepository;

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

        LocalDateTime start = null;
        LocalDateTime end   = null;
        if (date != null) {
            start = date.atStartOfDay();
            end   = date.plusDays(1).atStartOfDay();
        }

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

    @Transactional
    public ExtractTasksResponse extractFromTranscript(Long userId, Long meetingId, ExtractTasksRequest req) {
        boolean dry = req != null && Boolean.TRUE.equals(req.getDryRun());
        boolean overwrite = req != null && Boolean.TRUE.equals(req.getOverwrite());
        boolean includeAgendas = (req == null) || Boolean.TRUE.equals(req.getIncludeAgendas());
        String language = (req != null && req.getLanguage() != null) ? req.getLanguage() : "ko";
        Integer defaultDueDays = (req != null && req.getDefaultDueDays() != null) ? req.getDefaultDueDays() : 7;

        // 권한/소속 검증
        User user = getUserOrThrow(userId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MEETING_NOT_FOUND));
        Team team = getTeamOrThrow(meeting.getTeam().getId());
        checkMembershipOrThrow(user, team);

        String transcript = meeting.getTranscript();
        if (transcript == null || transcript.isBlank()) {
            throw new AppException(TaskErrorCode.INVALID_REQUEST);
        }

        var participants = participantRepository.findAllByMeetingId(meetingId);

        java.util.List<com.example.dotdot.domain.Agenda> agendas = includeAgendas
                ? agendaRepository.findAllByMeetingId(meetingId)
                : java.util.Collections.emptyList();

        String participantLines = participants.stream()
                .map(p -> "- " + p.getUser().getName())
                .collect(java.util.stream.Collectors.joining("\n"));

        String agendaText = agendas.isEmpty() ? "(없음)" :
                agendas.stream()
                        .map(a -> "- " + a.getAgenda()
                                + ((a.getBody() == null || a.getBody().isBlank()) ? "" : ("\n  " + a.getBody())))
                        .collect(java.util.stream.Collectors.joining("\n"));

        String prompt = buildTaskPrompt(transcript, participantLines, agendaText, language);

        java.util.List<TaskDraft> drafts = taskExtractClient.extract(prompt);

        if (overwrite && !dry) {
            taskRepository.deleteByMeeting_Id(meetingId);
        }

        java.util.Map<String, UserTeam> nameToUserTeam = new java.util.HashMap<>();
        for (var p : participants) {
            userTeamRepository.findByTeam_IdAndUser_Id(team.getId(), p.getUser().getId())
                    .ifPresent(ut -> nameToUserTeam.put(normalizeName(p.getUser().getName()), ut));
        }

        int created = 0, skipped = 0;
        java.util.List<TaskDraft> normalizedDrafts = new java.util.ArrayList<>();

        for (TaskDraft d : drafts) {
            String inTitle = d.getTitle();
            if (isBlank(inTitle)) { skipped++; continue; }

            UserTeam ut = nameToUserTeam.get(normalizeName(d.getAssigneeName()));
            if (ut == null) { skipped++; continue; }

            java.time.LocalDateTime due = parseIsoOrDefault(d.getDue(), meeting.getMeetingAt(), defaultDueDays);
            TaskPriority priority = normalizePriority(d.getPriority()); // HIGH/MEDIUM/LOW

            String title = cut(inTitle, 200);
            String desc  = cut(d.getDescription(), 4000);

            if (dry) {
                normalizedDrafts.add(new TaskDraft(
                        ut.getUser().getName(),
                        title,
                        desc,
                        toIsoString(due),
                        (priority == null ? TaskPriority.MEDIUM : priority).name()
                ));
            } else {
                Task saved = Task.of(
                        team,
                        meeting,
                        ut,
                        title,
                        desc,
                        (priority == null ? TaskPriority.MEDIUM : priority),
                        TaskStatus.TODO,
                        due
                );
                taskRepository.save(saved);
                created++;
            }
        }

        java.util.List<TaskDraft> payload = dry ? normalizedDrafts : java.util.Collections.<TaskDraft>emptyList();
        return new ExtractTasksResponse(meetingId, created, skipped, payload);
    }

    private String buildTaskPrompt(String transcript, String participantLines, String agendaText, String language) {
        return """
            당신은 회의록에서 실행 가능한 TODO 태스크를 추출하는 도우미입니다.

            [출력 언어]
            - 반드시 %s 로 작성하세요.

            [회의 참가자]
            %s

            [아젠다]
            %s

            [회의록]
            %s

            [출력 형식]
            JSON 배열로 출력하며, 각 항목은 다음 속성을 가집니다:
            - title: 짧고 명확한 실행 항목 제목
            - description: (선택) 추가 설명
            - assigneeName: 반드시 위 참가자 중 한 명의 이름
            - due: ISO-8601 날짜/시간 (예측 불가능하면 null)
            - priority: HIGH | MEDIUM | LOW (기본 MEDIUM)
            """.formatted(language, participantLines, agendaText, transcript);
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

    private static boolean isBlank(String s){
        return s == null || s.isBlank();
    }

    private static String cut(String s, int max){
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static String normalizeName(String name){
        if (name == null) return null;
        // 공백/구두점 제거, “님/씨” 접미사 제거, 소문자화
        return name.replaceAll("[\\s\\p{Punct}]", "")
                .replaceAll("(님|씨)$","")
                .toLowerCase();
    }

    private static java.time.LocalDateTime parseIsoOrDefault(
            String iso, java.time.LocalDateTime meetingAt, Integer plusDays
    ){
        if (iso != null && !iso.isBlank()) {
            try {
                return java.time.OffsetDateTime.parse(iso).toLocalDateTime();
            } catch (Exception ignore) { /* fall through */ }
        }
        int days = (plusDays == null || plusDays < 0) ? 7 : plusDays;
        return (meetingAt != null) ? meetingAt.plusDays(days)
                : java.time.LocalDateTime.now().plusDays(days);
    }

    private static TaskPriority normalizePriority(String p){
        if (p == null) return null;
        try { return TaskPriority.valueOf(p.toUpperCase()); }
        catch (Exception e){ return null; }
    }
    private static String toIsoString(java.time.LocalDateTime ldt) {
        if (ldt == null) return null;
        var zone = java.time.ZoneId.systemDefault();
        return ldt.atZone(zone).toOffsetDateTime().toString();
    }
}


