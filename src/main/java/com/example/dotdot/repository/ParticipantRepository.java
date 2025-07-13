package com.example.dotdot.repository;

import com.example.dotdot.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    int countByMeetingId(Long meetingId);
}
