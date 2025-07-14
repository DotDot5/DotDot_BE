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

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private String title;

    @Column(name = "meeting_at", nullable = false)
    private LocalDateTime meetingAt;

    @Lob
    private String transcript;

    @Lob
    private String summary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_method", nullable = false)
    private MeetingMethod meetingMethod;

    private Integer duration;

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
