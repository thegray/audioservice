package org.example.audioservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "phrase_tab")
public class PhraseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;
}
