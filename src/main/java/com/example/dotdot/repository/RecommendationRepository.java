package com.example.dotdot.repository;

import com.example.dotdot.domain.Meeting;
import com.example.dotdot.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation,Long> {
    List<Recommendation> findAllByMeetingOrderByPriorityAsc(Meeting meeting);

    void deleteAllByMeetingId(Long meetingId);
}
