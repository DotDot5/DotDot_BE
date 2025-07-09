package com.example.dotdot.service;

import com.example.dotdot.domain.Team;
import com.example.dotdot.domain.User;
import com.example.dotdot.domain.UserTeam;
import com.example.dotdot.dto.request.team.AddTeamMemberRequest;
import com.example.dotdot.dto.request.team.CreateTeamRequest;
import com.example.dotdot.dto.request.team.TeamNoticeRequest;
import com.example.dotdot.dto.request.team.UserRoleUpdateRequest;
import com.example.dotdot.dto.response.team.TeamDetailResponse;
import com.example.dotdot.dto.response.team.TeamMemberResponse;
import com.example.dotdot.dto.response.team.TeamNoticeResponse;
import com.example.dotdot.dto.response.team.TeamResponse;
import com.example.dotdot.global.exception.team.AlreadyJoinedTeamException;
import com.example.dotdot.global.exception.team.ForbiddenTeamAccessException;
import com.example.dotdot.global.exception.team.TeamNotFoundException;
import com.example.dotdot.global.exception.team.UserNotInTeamException;
import com.example.dotdot.global.exception.user.UserNotFoundException;
import com.example.dotdot.repository.TeamRepository;
import com.example.dotdot.repository.UserRepository;
import com.example.dotdot.repository.UserTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.dotdot.global.exception.user.UserErrorCode.*;
import static com.example.dotdot.global.exception.team.TeamErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServcie {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;

    /**
     * 팀 생성
     */
    @Transactional
    public Long createTeam(Long userId, CreateTeamRequest request){
        User user = getUserOrThrow(userId);
        Team team = Team.create(request.getTeamName());
        teamRepository.save(team);

        UserTeam userTeam = UserTeam.builder()
                .user(user)
                .team(team)
                .role("")  // 초기 역할 없음
                .build();
        userTeamRepository.save(userTeam);

        return team.getId();
    }

    /**
     * 팀 공지 수정
     */
    @Transactional
    public void updateTeamNotice(Long userId, Long teamId, TeamNoticeRequest request){
        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        team.updateNotice(request.getNotice());
    }
    /**
     * 팀 공지 조회
     */
    @Transactional(readOnly = true)
    public TeamNoticeResponse getTeamNotice(Long userId, Long teamId) {
        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        return new TeamNoticeResponse(team.getNotice());
    }

    /**
     * 팀원 추가
     */
    @Transactional
    public void addTeamMember(Long userId, Long teamId, AddTeamMemberRequest request){
        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));

        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        if (userTeamRepository.existsByUserAndTeam(invitedUser, team)) {
            throw new AlreadyJoinedTeamException(ALREADY_JOINED_TEAM);
        }
        UserTeam userTeam = UserTeam.builder()
                .user(invitedUser)
                .team(team)
                .role("")  // 초기 역할 없음
                .build();

        userTeamRepository.save(userTeam);
    }
    /**
     * 팀원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long userId,Long teamId) {
        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        return userTeamRepository.findByTeam(team).stream()
                .map(ut -> TeamMemberResponse.builder()
                        .userId(ut.getUser().getId())
                        .name(ut.getUser().getName())
                        .profileImageUrl(ut.getUser().getProfileImageUrl())
                        .role(ut.getRole())
                        .build())
                .collect(Collectors.toList());
    }
    /**
     * 팀원 역할 수정
     */
    @Transactional
    public void updateUserRole(Long userId,Long teamId, Long targetUserId, UserRoleUpdateRequest request){
        User targetUser = getUserOrThrow(targetUserId);

        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        UserTeam userTeam = userTeamRepository.findByUserAndTeam(targetUser, team)
                .orElseThrow(() -> new UserNotInTeamException(USER_NOT_IN_TEAM));
        userTeam.changeRole(request.getRole());
    }

    /**
     * 내가 속한 팀 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(Long userId) {
        User user = getUserOrThrow(userId);

        return userTeamRepository.findByUserOrderByTeamCreatedAtAsc(user).stream()
                .map(ut -> new TeamResponse(
                        ut.getTeam().getId(),
                        ut.getTeam().getName()
                ))
                .collect(Collectors.toList());
    }
    /**
     * 특정 팀 상세 조회
     * 특정 팀 상세 조회
     */
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamDetail(Long userId,Long teamId) {
        User user = getUserOrThrow(userId);
        Team team = getTeamOrThrow(teamId);
        checkMembershipOrThrow(user, team);

        List<TeamMemberResponse> members = userTeamRepository.findByTeam(team).stream()
                .map(ut -> TeamMemberResponse.builder()
                        .userId(ut.getUser().getId())
                        .name(ut.getUser().getName())
                        .profileImageUrl(ut.getUser().getProfileImageUrl())
                        .role(ut.getRole())
                        .build())
                .collect(Collectors.toList());

        return new TeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getNotice(),
                members
        );
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND));
    }

    private Team getTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND));
    }

    private void checkMembershipOrThrow(User user, Team team) {
        if (!userTeamRepository.existsByUserAndTeam(user, team)) {
            throw new ForbiddenTeamAccessException(FORBIDDEN_TEAM_ACCESS);
        }
    }


}
