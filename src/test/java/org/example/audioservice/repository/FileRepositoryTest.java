package org.example.audioservice.repository;

import org.example.audioservice.model.FileEntity;
import org.example.audioservice.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    void shouldFindLatestFile() {
        // Arrange
        FileEntity file = fileRepository.save(FileEntity.builder()
                .userId(100L)
                .phraseId(200L)
                .fileName("test.mp3")
                .format("mp3")
                .createdAt(System.currentTimeMillis())
                .isOriginal(true)
                .build());

        // Act
        Optional<FileEntity> result = fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "mp3");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(file.getFileName(), result.get().getFileName());
    }
}
