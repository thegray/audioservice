package org.example.audioservice.repository;

import org.example.audioservice.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(Long userId, Long phraseId, String format);
    Optional<FileEntity> findTopByUserIdAndPhraseIdAndOriginalOrderByCreatedAtDesc(Long userId, Long phraseId, boolean original);
}
