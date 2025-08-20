package com.example.dotdot.controller;

import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.dto.ErrorResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "BookmarkController", description = "Bookmark 관련 API")
public interface BookmarkControllerSpecification {

    @Operation(summary = "speechLog 북마크 추가/취소", description = "speechLog 북마크가 이미 존재하면 취소, 없으면 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 추가/취소 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 speechLog (BOOKMARK-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/speech-logs/{speechLogId}")
    public ResponseEntity<DataResponse<Void>> toggleBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long speechLogId);

    @Operation(summary = "특정 미팅의 북마크 목록 조회", description = "특정 미팅에서 현재 로그인한 사용자가 북마크한 모든 speechLog 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 미팅 (MEETING-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<DataResponse<List<Long>>> getMyBookmarksInMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long meetingId);
}
