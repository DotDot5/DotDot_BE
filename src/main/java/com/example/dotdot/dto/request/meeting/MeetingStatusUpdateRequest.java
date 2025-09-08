package com.example.dotdot.dto.request.meeting;

import com.example.dotdot.domain.Meeting.MeetingStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusUpdateRequest {
    private MeetingStatus status; // 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED'
}