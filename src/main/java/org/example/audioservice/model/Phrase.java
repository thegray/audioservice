package org.example.audioservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phrases_tab")
public class Phrase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
