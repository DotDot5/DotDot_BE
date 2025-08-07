package com.example.dotdot.service;

import com.example.dotdot.domain.*;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.dto.response.meeting.MeetingSttResultResponse; // ⭐️ 새로 추가된 GET 응답 DTO
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.AgendaRepository;
import com.example.dotdot.repository.MeetingRepository;
import com.example.dotdot.repository.ParticipantRepository;
import com.example.dotdot.repository.UserRepository;
import com.example.dotdot.repository.TeamRepository;
import com.example.dotdot.repository.UserTeamRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
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
                    return true;
                })
                .sorted((m1, m2) -> {
                    if ("finished".equalsIgnoreCase(statusFilter)) {
                        return m2.getMeetingAt().compareTo(m1.getMeetingAt());
                    } else if ("upcoming".equalsIgnoreCase(statusFilter)) {
                        return m1.getMeetingAt().compareTo(m2.getMeetingAt());
                    }
                    return 0;
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

    @Transactional
    public void updateMeetingSttResult(Long meetingId, Integer duration, String transcript) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        meeting.setDuration(duration);
        meeting.setTranscript(transcript);

        meetingRepository.save(meeting);
    }

    // ⭐️⭐️⭐️ STT 결과를 조회하는 메서드를 추가합니다. ⭐️⭐️⭐️
    @Transactional(readOnly = true)
    public MeetingSttResultResponse getMeetingSttResult(Long meetingId) {
        // meetingId로 회의를 찾습니다. 없으면 예외를 던집니다.
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 조회된 meeting 객체에서 필요한 정보를 추출하여 응답 DTO를 생성합니다.
        // 이 응답 DTO는 Next.js route.ts로 반환됩니다.
        return MeetingSttResultResponse.builder()
                .meetingId(meeting.getId())
                .transcript(meeting.getTranscript())
                .duration(meeting.getDuration())
                .build();
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }
}