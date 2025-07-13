package com.example.dotdot.repository;

import com.example.dotdot.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findAllByTeamId(Long teamId);
}
