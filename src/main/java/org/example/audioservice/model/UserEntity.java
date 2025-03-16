package org.example.audioservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_tab")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;
}
