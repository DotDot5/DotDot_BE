package com.example.dotdot.domain;

import com.example.dotdot.dto.response.chatbot.ChatResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bookmarks")
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "speech_log_id", nullable = false)
    private SpeechLog speechLog;

    // of 정적 메소드
    public static Bookmark of(User user, SpeechLog speechLog) {
        return Bookmark.builder()
                .user(user)
                .speechLog(speechLog)
                .build();
    }

}
