package com.example.dotdot.dto.response.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingSummaryStatusResponse {
    Long meetingId;
    String status;
    String summary;
    String updatedAt;
}
