package com.example.dotdot.service;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Participant;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.global.exception.meeting.MeetingErrorCode;
import com.example.dotdot.global.exception.meeting.MeetingNotFoundException;
import com.example.dotdot.repository.AgendaRepository;
import com.example.dotdot.repository.MeetingRepository;
import com.example.dotdot.repository.ParticipantRepository;
import com.google.api.gax.rpc.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final AgendaRepository agendaRepository;

    @Transactional
    public Long createMeeting(CreateMeetingRequest request) {
        Meeting meeting = meetingRepository.save(
                Meeting.builder()
                        .teamId(request.getTeamId())
                        .title(request.getTitle())
                        .meetingAt(request.getMeetingAt())
                        .createdAt(LocalDateTime.now())
                        .meetingMethod(request.getMeetingMethod())
                        .note(request.getNote())
                        .build()
        );

        List<ParticipantDto> participantList = request.getParticipants();
        for (ParticipantDto dto : participantList) {
            participantRepository.save(
                    Participant.builder()
                            .meeting(meeting)
                            .userId(dto.getUserId())
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
            participantRepository.save(
                    Participant.builder()
                            .meeting(meeting)
                            .userId(dto.getUserId())
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


}
