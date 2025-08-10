package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "meetings")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private String title;

    @Column(name = "meeting_at", nullable = false)
    private LocalDateTime meetingAt;

    @Lob
    private String transcript;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_method", nullable = false)
    private MeetingMethod meetingMethod;

    @Builder.Default
    private int duration = 0;

    @Column(name = "audio_id")
    private Long audioId;

    @Lob
    private String note;

    public enum MeetingMethod {
        RECORD, REALTIME
    }

    public void update(String title, LocalDateTime meetingAt, MeetingMethod meetingMethod, String note) {
        this.title = title;
        this.meetingAt = meetingAt;
        this.meetingMethod = meetingMethod;
        this.note = note;
    }

}
