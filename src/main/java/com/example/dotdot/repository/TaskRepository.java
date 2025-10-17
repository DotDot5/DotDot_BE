package com.example.dotdot.repository;

import com.example.dotdot.domain.UserTeam;
import com.example.dotdot.domain.task.Task;
import com.example.dotdot.domain.task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    void deleteByMeeting_Id(Long meetingId);

    @Query("""
      select t from Task t
      where t.team.id = :teamId
        and ( (:start is null and :end is null) or (t.due >= :start and t.due < :end) )
        and (:meetingId is null or t.meeting.id = :meetingId)
        and (:assigneeUserId is null or t.assignee.user.id = :assigneeUserId)
    """)
    Page<Task> searchTeamTasks(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("meetingId") Long meetingId,
            @Param("assigneeUserId") Long assigneeUserId,
            Pageable pageable
    );

    void deleteAllByMeetingId(Long meetingId);

    interface StatusCount {
        TaskStatus getStatus();
        long getCount();
    }

    @Query("""
      select t.status as status, count(t) as count
      from Task t
      where t.team.id = :teamId
        and ( (:start is null and :end is null) or (t.due >= :start and t.due < :end) )
        and (:meetingId is null or t.meeting.id = :meetingId)
        and (:assigneeUserId is null or t.assignee.user.id = :assigneeUserId)
      group by t.status
    """)
    List<StatusCount> countByStatusForTeam(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("meetingId") Long meetingId,
            @Param("assigneeUserId") Long assigneeUserId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.status = :status WHERE t.id IN :ids")
    int bulkUpdateStatus(@Param("ids") Collection<Long> ids, @Param("status") TaskStatus status);

    @Modifying
    @Query("UPDATE Task t SET t.assignee = null WHERE t.assignee IN :userTeams")
    void unassignTasksByUserTeams(@Param("userTeams") List<UserTeam> userTeams);


    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.assignee a " +
            "LEFT JOIN FETCH a.user " +
            "WHERE t.id = :taskId")
    Optional<Task> findTaskWithDetailsById(@Param("taskId") Long taskId);
}