package com.example.dotdot.dto.response.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MeetingListResponse {
    private Long meetingId;
    private String title;
    private LocalDateTime meetingAt;
    private int duration;
    private int participantCount;
}
