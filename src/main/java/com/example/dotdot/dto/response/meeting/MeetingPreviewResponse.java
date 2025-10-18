package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Participant;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.response.meeting.ParticipantResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPreviewResponse {
    private Long meetingId;
    private Long teamId;
    private String title;
    private ZonedDateTime meetingAt;
    private String meetingMethod;
    private String note;
    private List<ParticipantResponse> participants;
    private List<AgendaDto> agendas;
    private int duration;

    public static MeetingPreviewResponse from(Meeting meeting, List<Agenda> agendas, List<Participant> participants) {
        return MeetingPreviewResponse.builder()
                .meetingId(meeting.getId())
                .teamId(meeting.getTeam().getId())
                .title(meeting.getTitle())
                .meetingAt(meeting.getMeetingAt())
                .meetingMethod(meeting.getMeetingMethod().name())
                .note(meeting.getNote())
                .agendas(agendas.stream().map(AgendaDto::from).collect(Collectors.toList()))
                .participants(participants.stream().map(ParticipantResponse::from).collect(Collectors.toList()))
                .duration(meeting.getDuration())
                .build();
    }
}