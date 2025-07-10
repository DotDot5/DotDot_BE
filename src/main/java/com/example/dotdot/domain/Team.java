package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notice = "";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTeam> userTeams = new ArrayList<>();

    @Builder
    private Team(String name, String notice) {
        this.name = name;
        this.notice = notice;
        this.createdAt = LocalDateTime.now();
    }

    public static Team create(String name) {
        return Team.builder()
                .name(name)
                .notice("")
                .build();
    }
    public void updateNotice(String notice) {
        this.notice = notice;
    }
    public void updateName(String name) {
        this.name = name;
    }

}
