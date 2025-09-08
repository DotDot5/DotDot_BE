// SpeechLogRepository.java

package com.example.dotdot.repository;

import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.SpeechLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeechLogRepository extends JpaRepository<SpeechLog, Long> {
    // 회의에 속한 모든 SpeechLog를 삭제
    void deleteAllByMeeting(Meeting meeting);

    // meeting 객체를 인자로 받아 조회
    List<SpeechLog> findByMeeting(Meeting meeting);

    void deleteAllByMeetingId(Long meetingId);
}