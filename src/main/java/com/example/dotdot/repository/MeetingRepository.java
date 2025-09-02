package com.example.dotdot.repository;

import com.example.dotdot.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findAllByTeamId(Long teamId);
    List<Meeting> findByTeamIdIn(List<Long> teamIds);

    // 회의의 STT 결과(transcript)만 업데이트하는 쿼리
    @Modifying
    @Query("UPDATE Meeting m SET m.transcript = :transcript WHERE m.id = :id")
    void updateTranscript(@Param("id") Long id, @Param("transcript") String transcript);
}