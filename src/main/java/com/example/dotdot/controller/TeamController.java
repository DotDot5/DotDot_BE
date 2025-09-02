package com.example.dotdot.controller;

import com.example.dotdot.dto.request.team.*;
import com.example.dotdot.dto.response.team.TeamDetailResponse;
import com.example.dotdot.dto.response.team.TeamMemberResponse;
import com.example.dotdot.dto.response.team.TeamNoticeResponse;
import com.example.dotdot.dto.response.team.TeamResponse;
import com.example.dotdot.global.dto.DataResponse;
import com.example.dotdot.global.security.CustomUserDetails;
import com.example.dotdot.service.TeamServcie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController implements TeamControllerSpecification{
    private final TeamServcie teamService;

    @PostMapping
    public ResponseEntity<DataResponse<Long>> createTeam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateTeamRequest request
    ){
        Long teamId=teamService.createTeam(userDetails.getId(),request);
        return ResponseEntity.ok(DataResponse.from(teamId));
    }

    @PatchMapping("/{teamId}/notice")
    public ResponseEntity<DataResponse<Void>> updateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @RequestBody TeamNoticeRequest request
    ){
        teamService.updateTeamNotice(userDetails.getId(), teamId, request);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @GetMapping("/{teamId}/notice")
    public ResponseEntity<DataResponse<TeamNoticeResponse>> getNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    ) {
        TeamNoticeResponse notice = teamService.getTeamNotice(userDetails.getId(),teamId);
        return ResponseEntity.ok(DataResponse.from(notice));
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<DataResponse<String>> addMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request
    ){
        teamService.addTeamMember(userDetails.getId(),teamId, request);
        return ResponseEntity.ok(DataResponse.from("팀원 초대 성공."));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<DataResponse<List<TeamMemberResponse>>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    ){
        List<TeamMemberResponse> members = teamService.getTeamMembers(userDetails.getId(),teamId);
        return ResponseEntity.ok(DataResponse.from(members));
    }

    @PatchMapping("/{teamId}/members/{userId}/role")
    public ResponseEntity<DataResponse<Void>> updateUserRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest request
    ) {
        teamService.updateUserRole(userDetails.getId(),teamId, userId, request);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<DataResponse<List<TeamResponse>>> getMyTeams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TeamResponse> teams = teamService.getMyTeams(userDetails.getId());
        return ResponseEntity.ok(DataResponse.from(teams));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<DataResponse<TeamDetailResponse>> getTeamDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId
    ) {
        TeamDetailResponse detail = teamService.getTeamDetail(userDetails.getId(),teamId);
        return ResponseEntity.ok(DataResponse.from(detail));
    }
    @PatchMapping("/{teamId}/name")
    public ResponseEntity<DataResponse<Void>> updateTeamName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody UpdateTeamNameRequest request
    ){
        teamService.updateTeamName(userDetails.getId(),teamId, request);
        return ResponseEntity.ok(DataResponse.ok());
    }

}
