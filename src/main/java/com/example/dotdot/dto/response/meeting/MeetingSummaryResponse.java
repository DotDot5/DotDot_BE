package com.example.dotdot.dto.response.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingSummaryResponse {
    Long meetingId;
    String summary;
}
