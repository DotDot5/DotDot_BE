package com.example.dotdot.controller;

import com.example.dotdot.dto.request.team.*;
import com.example.dotdot.dto.response.team.TeamDetailResponse;
import com.example.dotdot.dto.response.team.TeamMemberResponse;
import com.example.dotdot.dto.response.team.TeamNoticeResponse;
import com.example.dotdot.dto.response.team.TeamResponse;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TeamController", description = "Team 관련 API")
public interface TeamControllerSpecification {

    @Operation(summary = "팀 생성", description = "팀 이름으로 새로운 팀 워크스페이스를 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DataResponse<Long>> createTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateTeamRequest request
    );

    @Operation(summary = "팀 공지 수정", description = "새로운 공지 내용으로 팀 공지를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 공지 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 (TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{teamId}/notice")
    public ResponseEntity<DataResponse<Void>> updateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestBody TeamNoticeRequest request
    );

    @Operation(summary = "팀 공지 조회", description = "팀 공지를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 공지 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 (TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{teamId}/notice")
    public ResponseEntity<DataResponse<TeamNoticeResponse>> getNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    ) ;

    @Operation(summary = "팀원 추가", description = "이메일을 통해 사용자를 조회하여 팀원을 추가합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 추가 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원 또는 팀 (USER-001 / TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "이미 팀에 가입된 사용자 (TEAM-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{teamId}/members")
    public ResponseEntity<DataResponse<String>> addMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request
    ) ;

    @Operation(summary = "팀원 목록 조회", description = "팀원의 목록(리스트)을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 (TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{teamId}/members")
    public ResponseEntity<DataResponse<List<TeamMemberResponse>>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    );

    @Operation(summary = "팀원 역할 수정", description = "팀원의 역할을 텍스트로 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 역할 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원 또는 소속횐 회원 못찾음 (USER-001 / TEAM-003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{teamId}/members/{userId}/role")
    public ResponseEntity<DataResponse<Void>> updateUserRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest request
    );

    @Operation(summary = "내가 속한 팀 목록", description = "내가 속한 팀의 목록을 리스트로 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 속한 팀 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원 (USER-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<DataResponse<List<TeamResponse>>> getMyTeams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) ;

    @Operation(summary = " 팀 상세 조회", description = "해당 teamId를 가진 팀의 상세 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 (TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{teamId}")
    public ResponseEntity<DataResponse<TeamDetailResponse>> getTeamDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    );

    @Operation(summary = " 팀 이름 수정", description = "해당 팀 워크스페이스의 이름을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 이름 수정 성공."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (USER-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 (TEAM-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "해당 팀에 접근 권한 없음(TEAM-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{teamId}/name")
    public ResponseEntity<DataResponse<Void>> updateTeamName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody UpdateTeamNameRequest request
    );
}
