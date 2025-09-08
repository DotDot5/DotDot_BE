package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


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
    private ZonedDateTime meetingAt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_status", nullable = false)
    @Builder.Default
    private SummaryStatus summaryStatus = SummaryStatus.NOT_STARTED;

    @Column(name = "summary_updated_at")
    private LocalDateTime summaryUpdatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_method", nullable = false)
    private MeetingMethod meetingMethod;

    @Builder.Default
    private int duration = 0;

    @Column(name = "audio_id")
    private String audioId;

    @Lob
    private String note;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpeechLog> speechLogs = new ArrayList<>();

    public enum MeetingStatus { SCHEDULED, IN_PROGRESS, FINISHED }
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    public enum MeetingMethod {
        RECORD, REALTIME
    }

    public enum SummaryStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }

    public void update(String title, ZonedDateTime meetingAt, MeetingMethod meetingMethod, String note) {
        this.title = title;
        this.meetingAt = meetingAt;
        this.meetingMethod = meetingMethod;
        this.note = note;
    }

    public void addSpeechLog(SpeechLog log) {
        this.speechLogs.add(log);
        log.setMeeting(this);
    }

    public void removeSpeechLog(SpeechLog log) {
        this.speechLogs.remove(log);
        log.setMeeting(null);
    }

    public ZonedDateTime getEndTime() {
        return meetingAt.plusMinutes(duration);
    }

    public void refreshStatusByTime(ZonedDateTime now) {
        if (now.isBefore(meetingAt)) {
            this.status = MeetingStatus.SCHEDULED;
        } else if (now.isAfter(getEndTime())) {
            this.status = MeetingStatus.FINISHED;
        } else {
            this.status = MeetingStatus.IN_PROGRESS;
        }
    }

    public void markFinished() {
        this.status = MeetingStatus.FINISHED;
    }
}



