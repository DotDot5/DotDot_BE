package com.example.dotdot.repository;

import com.example.dotdot.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findAllByMeetingId(Long meetingId);
    int countByMeetingId(Long meetingId);
}
