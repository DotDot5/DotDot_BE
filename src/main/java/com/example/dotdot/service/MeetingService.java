package com.example.dotdot.service;

import com.example.dotdot.domain.*;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.dto.response.meeting.MeetingSummaryResponse;
import com.example.dotdot.global.client.OpenAISummaryClient;
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.AgendaRepository;
import com.example.dotdot.repository.MeetingRepository;
import com.example.dotdot.repository.ParticipantRepository;
import com.example.dotdot.repository.UserRepository;
import com.example.dotdot.repository.TeamRepository;
import com.example.dotdot.repository.UserRepository;
import com.example.dotdot.repository.UserTeamRepository;
import com.google.api.gax.rpc.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public Long createMeeting(CreateMeetingRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.save(
                Meeting.builder()
                        .team(team)
                        .title(request.getTitle())
                        .meetingAt(request.getMeetingAt())
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
                            .speakerIndex(dto.getSpeakerIndex())  // 선택
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

    @Transactional
    public List<MeetingListResponse> getMeetingLists(Long teamId, String statusFilter) {
        List<Meeting> meetings = meetingRepository.findAllByTeamId(teamId);
        LocalDateTime now = LocalDateTime.now();

        return meetings.stream()
                .filter(meeting -> {
                    LocalDateTime endTime = meeting.getMeetingAt().plusMinutes(meeting.getDuration());
                    if ("upcoming".equalsIgnoreCase(statusFilter)) {
                        return now.isBefore(meeting.getMeetingAt());
                    } else if ("finished".equalsIgnoreCase(statusFilter)) {
                        return now.isAfter(endTime);
                    }
                    return true; // 필터 없으면 전체 반환
                })
                .sorted((m1, m2) -> {
                    if ("finished".equalsIgnoreCase(statusFilter)) {
                        return m2.getMeetingAt().compareTo(m1.getMeetingAt()); // 내림차순
                    } else if ("upcoming".equalsIgnoreCase(statusFilter)) {
                        return m1.getMeetingAt().compareTo(m2.getMeetingAt()); // 오름차순
                    }
                    return 0;
                })
                .map(meeting -> {
                    int count = participantRepository.countByMeetingId(meeting.getId());
                    String status = now.isBefore(meeting.getMeetingAt()) ? "upcoming" :
                            now.isAfter(meeting.getMeetingAt().plusMinutes(meeting.getDuration())) ? "finished" : "in_progress";
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

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getMyMeetingList(Long userId, String status, String sort) {
        User user = getUserOrThrow(userId);

        List<Long> teamIds = userTeamRepository.findByUserOrderByTeamCreatedAtAsc(user).stream()
                .map(ut -> ut.getTeam().getId())
                .collect(Collectors.toList());

        List<Meeting> meetings = meetingRepository.findByTeamIdIn(teamIds);

        LocalDateTime now = LocalDateTime.now();
        if ("finished".equalsIgnoreCase(status)) {
            meetings = meetings.stream()
                    .filter(m -> m.getMeetingAt().isBefore(now))
                    .collect(Collectors.toList());
        } else if ("upcoming".equalsIgnoreCase(status)) {
            meetings = meetings.stream()
                    .filter(m -> m.getMeetingAt().isAfter(now))
                    .collect(Collectors.toList());
        }

        Comparator<Meeting> comparator = Comparator.comparing(Meeting::getMeetingAt);
        if ("desc".equalsIgnoreCase(sort)) comparator = comparator.reversed();

        return meetings.stream()
                .sorted(comparator)
                .map(MeetingListResponse::from)
                .collect(Collectors.toList());
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
            // 필요시 에러타입 커스텀
            throw new IllegalStateException("회의 transcript가 없습니다. 업로드 후 다시 시도하세요.");
        }

        String summary = summaryClient.summarize(transcript);
        meeting.setSummary(summary);
        return summary;
    }

    @Transactional(readOnly = true)
    public String getMeetingSummary(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));
        return meeting.getSummary(); // null일 수 있음 → 컨트롤러에서 204 처리 고려
    }
}

