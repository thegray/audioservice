package org.example.audioservice.service;

import org.example.audioservice.dto.FileDownloadDTO;
import org.example.audioservice.exception.StorageException;
import org.example.audioservice.model.FileEntity;
import org.example.audioservice.repository.FileRepository;
import org.example.audioservice.service.FileService;
import org.example.audioservice.library.FFmpegWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FFmpegWrapper ffmpegWrapper;

    @InjectMocks
    private FileService fileService;

    private FileEntity fileEntity;
    private Path filePath;

    @BeforeEach
    void setup() throws Exception {
        filePath = Files.createTempFile("test-file", ".mp3");

        fileEntity = FileEntity.builder()
                .id(1L)
                .userId(100L)
                .phraseId(200L)
                .fileName("test.mp3")
                .filePath(filePath.toString())
                .format("mp3")
                .isOriginal(true)
                .createdAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void shouldReturnFileWhenExists() throws Exception {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "mp3"))
                .thenReturn(Optional.of(fileEntity));

        // Act
        FileDownloadDTO result = fileService.getAudioFile(100L, 200L, "mp3");

        // Assert
        assertNotNull(result);
        assertEquals("test.mp3", result.getFileName());
        assertNotNull(result.getFile());
    }

    @Test
    void shouldReturn404WhenFileNotFound() {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "mp3"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(StorageException.class, () -> fileService.getAudioFile(100L, 200L, "mp3"));
    }

    @Test
    void shouldConvertFileIfNotExists() throws Exception {
        // Arrange
        File convertedFile = Files.createTempFile("converted", ".wav").toFile();

        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "mp3"))
                .thenReturn(Optional.empty());

        when(fileRepository.findTopByUserIdAndPhraseIdAndIsOriginalOrderByCreatedAtDesc(100L, 200L, true))
                .thenReturn(Optional.of(fileEntity));

        when(ffmpegWrapper.convertAudio(any(), any())).thenReturn(convertedFile);

        // Act
        FileDownloadDTO result = fileService.getAudioFile(100L, 200L, "wav");

        // Assert
        assertNotNull(result);
        assertEquals("test.mp3", result.getFileName());
        verify(fileRepository).save(any());
    }

    @Test
    void shouldHandleConversionFailure() throws Exception {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "wav"))
                .thenReturn(Optional.empty());

        when(fileRepository.findTopByUserIdAndPhraseIdAndIsOriginalOrderByCreatedAtDesc(100L, 200L, true))
                .thenReturn(Optional.of(fileEntity));

        when(ffmpegWrapper.convertAudio(any(), any()))
                .thenThrow(new RuntimeException("Conversion failed"));

        // Act + Assert
        assertThrows(StorageException.class, () -> fileService.getAudioFile(100L, 200L, "wav"));
    }

    @Test
    void shouldReturn404WhenOriginalFileDeleted() {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(100L, 200L, "mp3"))
                .thenReturn(Optional.empty());

        when(fileRepository.findTopByUserIdAndPhraseIdAndIsOriginalOrderByCreatedAtDesc(100L, 200L, true))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(StorageException.class, () -> fileService.getAudioFile(100L, 200L, "mp3"));
    }
}
