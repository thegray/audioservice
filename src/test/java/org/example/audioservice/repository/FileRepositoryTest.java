package org.example.audioservice.repository;

import org.example.audioservice.model.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Rollback
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    private FileEntity file1;
    private FileEntity file2;

    @BeforeEach
    void setUp() {
        // init some test data
        file1 = FileEntity.builder()
                .userId(100L)
                .phraseId(200L)
                .fileName("test1.mp3")
                .filePath("/path/to/test1.mp3")
                .format("mp3")
                .groupId(1L)
                .createdAt(System.currentTimeMillis())
                .build();

        file2 = FileEntity.builder()
                .userId(100L)
                .phraseId(200L)
                .fileName("test2.wav")
                .filePath("/path/to/test2.wav")
                .format("wav")
                .groupId(1L)
                .createdAt(System.currentTimeMillis() + 1000)
                .build();

        fileRepository.save(file1);
        fileRepository.save(file2);
    }

    @Test
    void shouldFindLatestFileByUserIdAndPhraseId() {
        // WHEN
        Optional<FileEntity> latestFile = fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(100L, 200L);

        // THEN
        assertThat(latestFile).isPresent();
        assertThat(latestFile.get().getFileName()).isEqualTo("test2.wav");
    }

    @Test
    void shouldFindLatestFileByUserIdAndPhraseIdAndFormatAndGroupId() {
        // WHEN
        Optional<FileEntity> latestFile = fileRepository
                .findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(100L, 200L, "wav", 1L);

        // THEN
        assertThat(latestFile).isPresent();
        assertThat(latestFile.get().getFileName()).isEqualTo("test2.wav");
    }

    @Test
    void shouldFindOldestFileByUserIdAndPhraseIdAndGroupId() {
        // WHEN
        Optional<FileEntity> oldestFile = fileRepository
                .findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(100L, 200L, 1L);

        // THEN
        assertThat(oldestFile).isPresent();
        assertThat(oldestFile.get().getFileName()).isEqualTo("test1.mp3");
    }

    @Test
    void shouldReturnEmptyWhenFileNotFound() {
        // WHEN
        Optional<FileEntity> result = fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(999L, 999L);

        // THEN
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenFormatNotFound() {
        // WHEN
        Optional<FileEntity> result = fileRepository
                .findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(100L, 200L, "flac", 1L);

        // THEN
        assertThat(result).isNotPresent();
    }
}
