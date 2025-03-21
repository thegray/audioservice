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
@Table(name = "file_tab", indexes = {
        @Index(name = "idx_user_phrase_created_at", columnList = "user_id, phrase_id, created_at DESC"),
        @Index(name = "idx_user_phrase_format_group", columnList = "user_id, phrase_id, format, group_id"),
        @Index(name = "idx_user_phrase_group", columnList = "user_id, phrase_id, group_id")
})
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

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;
}
