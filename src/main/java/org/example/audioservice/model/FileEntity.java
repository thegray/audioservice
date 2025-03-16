package org.example.audioservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_tab")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String format;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phrase_id")
    private Long phraseId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;
}
