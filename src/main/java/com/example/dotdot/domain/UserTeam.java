package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="team_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String role = "";

    @Builder
    private UserTeam(User user, Team team, String role) {
        this.user = user;
        this.team = team;
        this.role = role;
    }
    public void changeRole(String role) {
        this.role = role;
    }

}
