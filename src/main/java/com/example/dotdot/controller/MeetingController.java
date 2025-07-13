package com.example.dotdot.controller;

import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.response.meeting.CreateMeetingResponse;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController implements MeetingControllerSpecification{

    private final MeetingService meetingService;

    @PostMapping
    @Override
    public ResponseEntity<DataResponse<CreateMeetingResponse>> createMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateMeetingRequest request
    ) {
        Long meetingId = meetingService.createMeeting(request);
        return ResponseEntity.ok(DataResponse.from(new CreateMeetingResponse(meetingId)));
    }

    @GetMapping("/{teamId}/list")
    public ResponseEntity<DataResponse<List<MeetingListResponse>>> getMeetingListByTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    ) {
        List<MeetingListResponse> meetings = meetingService.getMeetingLists(teamId);
        return ResponseEntity.ok(DataResponse.from(meetings));
    }
}
