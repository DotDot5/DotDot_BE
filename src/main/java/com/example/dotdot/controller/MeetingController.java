package com.example.dotdot.controller;

import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.request.meeting.MeetingStatusUpdateRequest;
import com.example.dotdot.dto.response.meeting.*;
import com.example.dotdot.dto.request.meeting.SttResultUpdateRequest;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.MeetingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody CreateMeetingRequest request) {
        Long meetingId = meetingService.createMeeting(userDetails.getId(),request);
        return ResponseEntity.ok(DataResponse.from(new CreateMeetingResponse(meetingId)));
    }

    @GetMapping("/{teamId}/list")
    public ResponseEntity<DataResponse<List<MeetingListResponse>>> getMeetingListByTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestParam(required = false) String status) {
        List<MeetingListResponse> meetings = meetingService.getMeetingLists(userDetails.getId(),teamId, status);
        return ResponseEntity.ok(DataResponse.from(meetings));
    }

    @GetMapping("/{meetingId}/preview")
    public ResponseEntity<DataResponse<MeetingPreviewResponse>> getMeetingPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId) {
        MeetingPreviewResponse response = meetingService.getMeetingPreview(userDetails.getId(),meetingId);
        return ResponseEntity.ok(DataResponse.from(response));
    }

    @PutMapping("/{meetingId}")
    @Override
    public ResponseEntity<DataResponse<Long>> updateMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestBody @Valid CreateMeetingRequest request) {
        Long updatedId = meetingService.updateMeeting(userDetails.getId(),meetingId, request);
        return ResponseEntity.ok(DataResponse.from(updatedId));
    }
    @PatchMapping("/{meetingId}/status")
    public ResponseEntity<DataResponse<Long>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestBody @Valid MeetingStatusUpdateRequest req
    ) {
        Long updatedId = meetingService.updateMeetingStatus(userDetails.getId(),meetingId, req.getStatus());
        return ResponseEntity.ok(DataResponse.from(updatedId));
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

    @Override
    @PostMapping("/{meetingId}/summarize")
    public ResponseEntity<MeetingSummaryResponse> summarize(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId
    ) {
        String summary = meetingService.summarizeMeeting(userDetails.getId(),meetingId);
        return ResponseEntity.ok(new MeetingSummaryResponse(meetingId, summary));
    }

    @Override
    @GetMapping("/{meetingId}/summary")
    public ResponseEntity<MeetingSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId
    ) {
        String summary = meetingService.getMeetingSummary(userDetails.getId(),meetingId);
        if (summary == null || summary.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new MeetingSummaryResponse(meetingId, summary));
    }

    @GetMapping("/{meetingId}/summary/status")
    public ResponseEntity<DataResponse<MeetingSummaryStatusResponse>> getSummaryStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId
    ) {
        var res = meetingService.getSummaryStatus(userDetails.getId(),meetingId);
        return ResponseEntity.ok(DataResponse.from(res));
    }

    @PutMapping("/{meetingId}/stt-result")
    public ResponseEntity<Void> updateMeetingSttResult(
            @PathVariable("meetingId") Long meetingId,
            @RequestBody SttResultUpdateRequest request) {
        try {
            System.out.println("Received request to update STT result for Meeting ID: " + meetingId);
            // 서비스 메서드 호출 시 meetingId와 request DTO를 모두 전달
            meetingService.updateMeetingSttResultAndSaveLogs(meetingId, request);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error fetching STT result for Meeting ID " + meetingId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<DataResponse<Void>> deleteMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId) {
        meetingService.deleteMeeting(userDetails.getId(), meetingId);
        return ResponseEntity.ok(DataResponse.ok());
    }

}