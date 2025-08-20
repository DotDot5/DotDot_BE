package com.example.dotdot.repository;

import com.example.dotdot.domain.task.Task;
import com.example.dotdot.domain.task.TaskPriority;
import com.example.dotdot.domain.task.TaskStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    void deleteByMeeting_Id(Long meetingId);

    @Query("""
      select t from Task t
      where t.team.id = :teamId
        and t.due >= :start and t.due < :end
        and (:meetingId is null or t.meeting.id = :meetingId)
        and (:assigneeUserId is null or t.assignee.user.id = :assigneeUserId)
    """)
    Page<Task> searchTeamTasks(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("meetingId") Long meetingId,                // 선택
            @Param("assigneeUserId") Long assigneeUserId,      // 선택(전체팀원= null)
            Pageable pageable
    );

    //진행 상황 요약
    interface StatusCount {
        TaskStatus getStatus();
        long getCount();
    }

    @Query("""
      select t.status as status, count(t) as count
      from Task t
      where t.team.id = :teamId
        and t.due >= :start and t.due < :end
        and (:meetingId is null or t.meeting.id = :meetingId)
        and (:assigneeUserId is null or t.assignee.user.id = :assigneeUserId)
      group by t.status
    """)
    List<StatusCount> countByStatusForTeam(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("meetingId") Long meetingId,               // 선택
            @Param("assigneeUserId") Long assigneeUserId      // 선택
    );
    /**
     * 상태 대량 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.status = :status WHERE t.id IN :ids")
    int bulkUpdateStatus(@Param("ids") Collection<Long> ids, @Param("status") TaskStatus status);
}
