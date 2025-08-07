package com.example.dotdot.controller;

import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.SttResultUpdateRequest;
import com.example.dotdot.dto.response.meeting.CreateMeetingResponse;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.dto.response.meeting.MeetingSttResultResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController implements MeetingControllerSpecification {

    private final MeetingService meetingService;

    // POST, GET, PUT 등 기존 API는 생략

    @PostMapping
    @Override
    public ResponseEntity<DataResponse<CreateMeetingResponse>> createMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateMeetingRequest request) {
        Long meetingId = meetingService.createMeeting(request);
        return ResponseEntity.ok(DataResponse.from(new CreateMeetingResponse(meetingId)));
    }

    @GetMapping("/{teamId}/list")
    public ResponseEntity<DataResponse<List<MeetingListResponse>>> getMeetingListByTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestParam(required = false) String status) {
        List<MeetingListResponse> meetings = meetingService.getMeetingLists(teamId, status);
        return ResponseEntity.ok(DataResponse.from(meetings));
    }

    @GetMapping("/{meetingId}/preview")
    public ResponseEntity<DataResponse<MeetingPreviewResponse>> getMeetingPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId) {
        MeetingPreviewResponse response = meetingService.getMeetingPreview(meetingId);
        return ResponseEntity.ok(DataResponse.from(response));
    }

    @GetMapping("/my")
    public ResponseEntity<DataResponse<List<MeetingListResponse>>> getMyMeetingList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Long userId = userDetails.getId();
        List<MeetingListResponse> meetings = meetingService.getMyMeetingList(userId, status, sort);
        return ResponseEntity.ok(DataResponse.from(meetings));
    }

    @PutMapping("/{meetingId}")
    @Override
    public ResponseEntity<DataResponse<Long>> updateMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestBody @Valid CreateMeetingRequest request) {
        Long updatedId = meetingService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(DataResponse.from(updatedId));
    }

    @PutMapping("/{meetingId}/stt-result")
    public ResponseEntity<Void> updateMeetingSttResult(
            @PathVariable("meetingId") Long meetingId,
            @RequestBody SttResultUpdateRequest request) { // DTO를 별도 파일로 분리했다고 가정
        try {
            System.out.println("Received request to update STT result for Meeting ID: " + meetingId);
            meetingService.updateMeetingSttResult(meetingId, request.getDuration(), request.getTranscript());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            System.err.println("Meeting not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error updating STT result for Meeting ID " + meetingId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{meetingId}/stt-result")
    public ResponseEntity<DataResponse<MeetingSttResultResponse>> getMeetingSttResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("meetingId") Long meetingId) {
        try {
            System.out.println("Received request to get STT result for Meeting ID: " + meetingId);
            MeetingSttResultResponse result = meetingService.getMeetingSttResult(meetingId);

            return ResponseEntity.ok(DataResponse.from(result));
        } catch (EntityNotFoundException e) {
            System.err.println("Meeting not found with ID: " + meetingId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 응답
        } catch (Exception e) {
            System.err.println("Error fetching STT result for Meeting ID " + meetingId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 응답
        }
    }
}