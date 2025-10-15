package com.example.dotdot.service;

import com.example.dotdot.domain.*;
import com.example.dotdot.dto.request.meeting.SttResultUpdateRequest;
import com.example.dotdot.dto.request.meeting.SpeechLogDto;
import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.repository.*;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.dto.response.meeting.MeetingSttResultResponse;
import com.example.dotdot.dto.response.meeting.MeetingSummaryResponse;
import com.example.dotdot.dto.response.meeting.MeetingSummaryStatusResponse;
import com.example.dotdot.global.client.OpenAISummaryClient;
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime; // [수정] LocalDateTime -> ZonedDateTime
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


import static com.example.dotdot.global.exception.meeting.MeetingErrorCode.MEETING_DELETE_FORBIDDEN;
import static com.example.dotdot.global.exception.user.UserErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final AgendaRepository agendaRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final OpenAISummaryClient summaryClient;
    private final SpeechLogRepository speechLogRepository;
    private final RecommendationRepository recommendationRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public Long createMeeting(CreateMeetingRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.save(
                Meeting.builder()
                        .team(team)
                        .title(request.getTitle())
                        .meetingAt(request.getMeetingAt())
                        // [수정] createdAt 필드도 ZonedDateTime으로 변경되었다고 가정합니다.
                        // 만약 Instant 타입이라면 Instant.now()를 사용하세요.
                        .createdAt(LocalDateTime.now())
                        .meetingMethod(request.getMeetingMethod())
                        .note(request.getNote())
                        .build()
        );

        List<ParticipantDto> participantList = request.getParticipants();
        for (ParticipantDto dto : participantList)
        {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));

            participantRepository.save(
                    Participant.builder()
                            .meeting(meeting)
                            .user(user)
                            .part(dto.getPart())
                            .speakerIndex(dto.getSpeakerIndex())
                            .build()
            );
        }

        List<AgendaDto> agendaList = request.getAgendas();
        for (AgendaDto dto : agendaList) {
            agendaRepository.save(
                    Agenda.builder()
                            .meeting(meeting)
                            .agenda(dto.getAgenda())
                            .body(dto.getBody())
                            .build()
            );
        }

        return meeting.getId();
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMeetingLists(Long teamId, String statusFilter) {
        List<Meeting> meetings = meetingRepository.findAllByTeamId(teamId);
        ZonedDateTime now = ZonedDateTime.now();

        Meeting.MeetingStatus parsedStatus = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            String s = statusFilter.trim().toUpperCase();
            try {
                parsedStatus = Meeting.MeetingStatus.valueOf(s);
            } catch (IllegalArgumentException ignore) {
                parsedStatus = null; // 잘못된 값이면 전체 반환
            }
        }
        final Meeting.MeetingStatus wanted = parsedStatus;
        return meetings.stream()
                .peek(m -> {
                    if (m.getStatus() == null) {
                        m.refreshStatusByTime(now);
                    }
                })
                .filter(m -> wanted == null || m.getStatus() == wanted)
                .sorted((m1, m2) -> {
                    if (wanted == Meeting.MeetingStatus.FINISHED) {
                        return m2.getMeetingAt().compareTo(m1.getMeetingAt());
                    } else if (wanted == Meeting.MeetingStatus.SCHEDULED) {
                        return m1.getMeetingAt().compareTo(m2.getMeetingAt());
                    } else {
                        return m1.getMeetingAt().compareTo(m2.getMeetingAt());
                    }
                })
                .map(meeting -> {
                    int count = participantRepository.countByMeetingId(meeting.getId());
                    return MeetingListResponse.builder()
                            .meetingId(meeting.getId())
                            .title(meeting.getTitle())
                            .meetingAt(meeting.getMeetingAt())
                            .duration(meeting.getDuration())
                            .participantCount(count)
                            .teamId(meeting.getTeam().getId())
                            .teamName(meeting.getTeam().getName())
                            .build();
                })
                .toList();
    }


    @Transactional
    public MeetingPreviewResponse getMeetingPreview(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        List<Agenda> agendas = agendaRepository.findAllByMeetingId(meetingId);
        List<Participant> participants = participantRepository.findAllByMeetingId(meetingId);

        return MeetingPreviewResponse.from(meeting, agendas, participants);
    }

    @Transactional
    public Long updateMeeting(Long meetingId, CreateMeetingRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        // meeting.update() 메소드도 ZonedDateTime을 받도록 수정되었다고 가정합니다.
        meeting.update(
                request.getTitle(),
                request.getMeetingAt(),
                request.getMeetingMethod(),
                request.getNote()
        );

        participantRepository.deleteAllByMeetingId(meetingId);
        agendaRepository.deleteAllByMeetingId(meetingId);

        for (ParticipantDto dto : request.getParticipants()) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));

            participantRepository.save(
                    Participant.builder()
                            .meeting(meeting)
                            .user(user)
                            .part(dto.getPart())
                            .speakerIndex(dto.getSpeakerIndex())
                            .build()
            );
        }

        for (AgendaDto dto : request.getAgendas()) {
            agendaRepository.save(
                    Agenda.builder()
                            .meeting(meeting)
                            .agenda(dto.getAgenda())
                            .body(dto.getBody())
                            .build()
            );
        }

        return meetingId;
    }

    @Transactional
    public Long updateMeetingStatus(Long meetingId, Meeting.MeetingStatus status) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        if (meeting.getStatus() == Meeting.MeetingStatus.FINISHED && status != Meeting.MeetingStatus.FINISHED) {
            throw new IllegalStateException("이미 종료된 회의 상태는 되돌릴 수 없습니다.");
        }

        meeting.setStatus(status);
        return meeting.getId();
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMyMeetingList(Long userId, String status, String sort) {
        User user = getUserOrThrow(userId);

        List<Long> teamIds = userTeamRepository.findByUserOrderByTeamCreatedAtAsc(user).stream()
                .map(ut -> ut.getTeam().getId())
                .collect(Collectors.toList());

        List<Meeting> meetings = meetingRepository.findByTeamIdIn(teamIds);

        if (status != null && !status.isBlank()) {
            String s = status.trim().toUpperCase();
            meetings = meetings.stream()
                    .filter(m -> {
                        switch (s) {
                            case "SCHEDULED":
                            case "upcoming":
                                return m.getStatus() == Meeting.MeetingStatus.SCHEDULED;
                            case "IN_PROGRESS":
                                return m.getStatus() == Meeting.MeetingStatus.IN_PROGRESS;
                            case "FINISHED":
                                return m.getStatus() == Meeting.MeetingStatus.FINISHED;
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());
        }

        Comparator<Meeting> comparator = Comparator.comparing(Meeting::getMeetingAt);
        if ("desc".equalsIgnoreCase(sort)) comparator = comparator.reversed();

        return meetings.stream()
                .sorted(comparator)
                .map(MeetingListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMeetingSttResultAndSaveLogs(Long meetingId, SttResultUpdateRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with ID: " + meetingId));

        meeting.setDuration(request.getDuration());
        meeting.setTranscript(request.getTranscript());
        meeting.setAudioId(request.getAudioId());
        speechLogRepository.deleteAllByMeeting(meeting);

        if (request.getSpeechLogs() != null && !request.getSpeechLogs().isEmpty()) {
            List<SpeechLog> speechLogs = request.getSpeechLogs().stream()
                    .map(logDto -> SpeechLog.builder()
                            .meeting(meeting)
                            .speakerIndex(logDto.getSpeakerIndex())
                            .text(logDto.getText())
                            .startTime(logDto.getStartTime())
                            .endTime(logDto.getEndTime())
                            .build())
                    .collect(Collectors.toList());

            speechLogRepository.saveAll(speechLogs);
        }
    }

    @Transactional(readOnly = true)
    public MeetingSttResultResponse getMeetingSttResult(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with ID: " + meetingId));

        List<SpeechLog> speechLogs = speechLogRepository.findByMeeting(meeting);

        List<SpeechLogDto> speechLogDtos = speechLogs.stream()
                .map(log -> {
                    SpeechLogDto dto = new SpeechLogDto();
                    dto.setSpeechLogId(log.getId());
                    dto.setSpeakerIndex(log.getSpeakerIndex());
                    dto.setText(log.getText());
                    dto.setStartTime(log.getStartTime());
                    dto.setEndTime(log.getEndTime());
                    return dto;
                })
                .collect(Collectors.toList());

        return MeetingSttResultResponse.builder()
                .meetingId(meeting.getId())
                .transcript(meeting.getTranscript())
                .duration(meeting.getDuration())
                .audioId(meeting.getAudioId())
                .speechLogs(speechLogDtos)
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }

    @Transactional
    public String summarizeMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        String transcript = meeting.getTranscript();
        if (transcript == null || transcript.isBlank()) {
            meeting.setSummaryStatus(Meeting.SummaryStatus.NOT_STARTED);
            meeting.setSummaryUpdatedAt(LocalDateTime.now());
            throw new IllegalStateException("회의 transcript가 없습니다. 업로드 후 다시 시도하세요.");
        }

        meeting.setSummaryStatus(Meeting.SummaryStatus.IN_PROGRESS);
        meeting.setSummaryUpdatedAt(LocalDateTime.now());

        try {
            List<Agenda> agendas = agendaRepository.findAllByMeetingId(meetingId);
            StringBuilder agendaText = new StringBuilder("회의 안건 목록:\n");
            for (Agenda a : agendas) {
                agendaText.append("- ").append(a.getAgenda()).append("\n");
                if (a.getBody() != null && !a.getBody().isBlank()) {
                    agendaText.append("  내용: ").append(a.getBody()).append("\n");
                }
            }
            String fullText = transcript + "\n\n" + agendaText;

            String summary = summaryClient.summarize(fullText);

            meeting.setSummary(summary);
            meeting.setSummaryStatus(Meeting.SummaryStatus.COMPLETED);
            meeting.setSummaryUpdatedAt(LocalDateTime.now());
            return summary;

        } catch (Exception e) {
            meeting.setSummaryStatus(Meeting.SummaryStatus.FAILED);
            meeting.setSummaryUpdatedAt(LocalDateTime.now());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public String getMeetingSummary(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));
        return meeting.getSummary();
    }

    @Transactional(readOnly = true)
    public MeetingSummaryStatusResponse getSummaryStatus(Long meetingId) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        // ZonedDateTime의 toString()은 ISO 8601 표준 형식을 반환하므로 안전합니다.
        return new MeetingSummaryStatusResponse(
                m.getId(),
                m.getSummaryStatus().name(),
                m.getSummaryStatus() == Meeting.SummaryStatus.COMPLETED ? m.getSummary() : null,
                m.getSummaryUpdatedAt() == null ? null : m.getSummaryUpdatedAt().toString()
        );
    }

    @Transactional
    public void deleteMeeting(Long userId, Long meetingId) {
        User user = getUserOrThrow(userId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));
        Team team = meeting.getTeam();
        userTeamRepository.findByTeam(team).stream()
                .filter(ut -> ut.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AppException(MEETING_DELETE_FORBIDDEN));
        agendaRepository.deleteAllByMeetingId(meetingId);
        participantRepository.deleteAllByMeetingId(meetingId);
        meetingRepository.deleteById(meetingId);
        recommendationRepository.deleteAllByMeetingId(meetingId);
        taskRepository.deleteAllByMeetingId(meetingId);
    }
}