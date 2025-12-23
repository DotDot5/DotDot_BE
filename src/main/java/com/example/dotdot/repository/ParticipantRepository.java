package com.example.dotdot.repository;

import com.example.dotdot.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findAllByMeetingId(Long meetingId);

    int countByMeetingId(Long meetingId);

    void deleteAllByMeetingId(Long meetingId);

    @Query("SELECT p FROM Participant p JOIN FETCH p.user WHERE p.meeting.id = :meetingId")
    List<Participant> findAllWithUserByMeetingId(@Param("meetingId") Long meetingId);
}

