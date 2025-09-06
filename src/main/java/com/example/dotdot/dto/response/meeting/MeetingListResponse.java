package com.example.dotdot.dto.response.meeting;

import com.example.dotdot.domain.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MeetingListResponse {
    private Long meetingId;
    private String title;
    private ZonedDateTime meetingAt;
    private int duration;
    private int participantCount;
    private Long teamId;
    private String teamName;

    public static MeetingListResponse from(Meeting meeting) {
        return MeetingListResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingAt(meeting.getMeetingAt())
                .duration(meeting.getDuration())
                .participantCount(0)
                .teamId(meeting.getTeam() != null ? meeting.getTeam().getId() : null)
                .teamName(meeting.getTeam().getName())
                .build();
    }
}
