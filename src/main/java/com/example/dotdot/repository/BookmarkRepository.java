package com.example.dotdot.repository;

import com.example.dotdot.domain.Bookmark;
import com.example.dotdot.domain.SpeechLog;
import com.example.dotdot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndSpeechLog(User user, SpeechLog speechLog);

    @Query("SELECT b.speechLog.id FROM Bookmark b " +
            "WHERE b.user.id = :userId AND b.speechLog.meeting.id = :meetingId " +
            "ORDER BY b.speechLog.id ASC")
    List<Long> findBookmarkedSpeechLogsByUserAndMeeting(@Param("userId") Long userId, @Param("meetingId") Long meetingId);
}
