package com.example.dotdot.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;


@Entity
@Table(name = "agendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agenda_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Lob
    @Column(name = "agenda", columnDefinition = "LONGTEXT",nullable = false)
    private String agenda;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String body;
}
