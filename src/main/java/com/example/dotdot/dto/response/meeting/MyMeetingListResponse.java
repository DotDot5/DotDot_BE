package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.domain.Meeting;

import java.time.LocalDateTime;

public class MyMeetingListResponse {
    public record MeetingListResponse(
            Long meetingId,
            String teamName,
            String title,
            LocalDateTime meetingAt
    ) {
        public static MeetingListResponse from(Meeting meeting) {
            return new MeetingListResponse(
                    meeting.getId(),
                    meeting.getTeam().getName(),
                    meeting.getTitle(),
                    meeting.getMeetingAt()
            );
        }
    }

}
