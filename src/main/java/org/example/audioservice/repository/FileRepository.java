package org.example.audioservice.repository;

import org.example.audioservice.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(Long userId, Long phraseId);
    Optional<FileEntity> findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(Long userId, Long phraseId, String format, Long groupId);
}
