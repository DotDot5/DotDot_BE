package com.example.dotdot.repository;

import com.example.dotdot.domain.Team;
import com.example.dotdot.domain.User;
import com.example.dotdot.domain.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {
    //유저가 속한 팀 목록 조회
    List<UserTeam> findByUser(User user);

    //팀에 속한 유저 목록 조회
    List<UserTeam> findByTeam(Team team);

    //역할 변경 시 활용할 특정 관계 조회
    Optional<UserTeam> findByUserAndTeam(User user, Team team);

    Optional<UserTeam> findByTeam_IdAndUser_Id(Long teamId, Long userId);
    //중복 참가 방지용 체크
    boolean existsByUserAndTeam(User user, Team team);

    List<UserTeam> findByUserOrderByTeamCreatedAtAsc(User user);

}
