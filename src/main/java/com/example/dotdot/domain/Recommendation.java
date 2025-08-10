package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "recommendations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Column(name = "priority")
    private int priority;

    public static Recommendation of(String title, String url, String description, Meeting meeting,int priority) {
        return Recommendation.builder()
                .title(title)
                .url(url)
                .description(description)
                .meeting(meeting)
                .priority(priority)
                .build();
    }

}
