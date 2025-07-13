package com.example.dotdot.repository;

import com.example.dotdot.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findAllByMeetingId(Long meetingId);
}
