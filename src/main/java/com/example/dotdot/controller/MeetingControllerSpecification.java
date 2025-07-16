package com.example.dotdot.controller;

import com.example.dotdot.dto.request.meeting.CreateMeetingRequest;
import com.example.dotdot.dto.response.meeting.CreateMeetingResponse;
import com.example.dotdot.dto.response.meeting.MeetingListResponse;
import com.example.dotdot.dto.response.meeting.MeetingPreviewResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.dto.ErrorResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MeetingController", description = "회의 관련 API")
public interface MeetingControllerSpecification {

    @Operation(summary = "회의 생성", description = "회의를 생성하고, 참가자와 안건을 함께 등록합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회의 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 또는 팀 (USER-001 / TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<DataResponse<CreateMeetingResponse>> createMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateMeetingRequest request
    );

    @Operation(summary = "팀 회의 목록 조회", description = "해당 팀의 모든 회의 목록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회의 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{teamId}/list")
    ResponseEntity<DataResponse<List<MeetingListResponse>>> getMeetingListByTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,

            @Parameter(description = "회의 상태 필터 (upcoming or finished)", example = "upcoming")
            @RequestParam(required = false) String status
    );

    @Operation(summary = "회의 정보 조회", description = "회의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회의 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회의 (MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{meetingId}/preview")
    ResponseEntity<DataResponse<MeetingPreviewResponse>> getMeetingPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId
    );

    @Operation(summary = "회의 수정", description = "회의 제목, 시간, 방식, 노트, 참여자, 안건을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{meetingId}")
    ResponseEntity<DataResponse<Long>> updateMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId,
            @RequestBody @Valid CreateMeetingRequest request
    );

    @Operation(summary = "나의 회의 목록 조회", description = "현재 로그인한 사용자가 속한 팀들의 회의 목록을 상태와 정렬 기준에 따라 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상적으로 회의 목록을 조회함"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회의 (MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<DataResponse<List<MeetingListResponse>>> getMyMeetingList(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "회의 상태 필터 (upcoming or finished)", example = "upcoming")
            @RequestParam(required = false) String status,

            @Parameter(description = "정렬 순서 (asc 또는 desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sort
    );
}
