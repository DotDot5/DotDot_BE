package com.example.dotdot.dto.request.meeting;

import com.example.dotdot.domain.Meeting;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMeetingRequest {
    private Long teamId;
    private String title;
    private LocalDateTime meetingAt;
    private Meeting.MeetingMethod meetingMethod;
    private String note;

    private List<ParticipantDto> participants;
    private List<AgendaDto> agendas;
}
