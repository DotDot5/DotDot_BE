package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "speech_logs")
public class SpeechLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "speech_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;


    @Column(name = "speaker_index", nullable = false)
    private int speakerIndex;

    @Lob
    @Column(name = "text", columnDefinition = "LONGTEXT")
    private String text;

    @Column(name = "start_time", nullable = false)
    private int startTime;

    @Column(name = "end_time", nullable = false)
    private int endTime;
}