package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.dto.request.meeting.SpeechLogDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MeetingSttResultResponse {
    private Long meetingId;
    private String transcript;
    private Integer duration;
    private List<SpeechLogDto> speechLogs;
}