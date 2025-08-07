package com.example.dotdot.dto.response.meeting;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingSttResultResponse {
    private Long meetingId;
    private String transcript;
    private Integer duration;
}