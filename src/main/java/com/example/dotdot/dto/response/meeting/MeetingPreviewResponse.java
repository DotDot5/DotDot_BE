package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.domain.Agenda;
import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Participant;
import com.example.dotdot.dto.request.meeting.AgendaDto;
import com.example.dotdot.dto.request.meeting.ParticipantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPreviewResponse {
    private Long meetingId;
    private String title;
    private LocalDateTime meetingAt;
    private String meetingMethod;
    private String note;
    private List<ParticipantDto> participants;
    private List<AgendaDto> agendas;

    public static MeetingPreviewResponse from(Meeting meeting, List<Agenda> agendas, List<Participant> participants) {
        return MeetingPreviewResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingAt(meeting.getMeetingAt())
                .meetingMethod(meeting.getMeetingMethod().name())
                .note(meeting.getNote())
                .agendas(agendas.stream().map(AgendaDto::from).collect(Collectors.toList()))
                .participants(participants.stream().map(ParticipantDto::from).collect(Collectors.toList()))
                .build();
    }
}
