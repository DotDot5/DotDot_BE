package com.example.dotdot.service;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Participant;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.repository.AgendaRepository;
import com.example.dotdot.repository.MeetingRepository;
import com.example.dotdot.repository.ParticipantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

        // 3. 안건 저장
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
    public List<MeetingListResponse> getMeetingLists(Long teamId) {
        List<Meeting> meetings = meetingRepository.findAllByTeamId(teamId);

        return meetings.stream()
                .map(meeting -> {
                    int count = participantRepository.countByMeetingId(meeting.getId());
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
}
