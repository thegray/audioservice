package org.example.audioservice.service;

import org.example.audioservice.dto.FileDownloadDTO;
import org.example.audioservice.exception.*;
import org.example.audioservice.library.FFmpegWrapper;
import org.example.audioservice.model.FileEntity;
import org.example.audioservice.repository.FileRepository;
import org.example.audioservice.service.FileService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
                .groupId(999L)
                .createdAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void shouldReturnLatestFileWhenExists() throws Exception {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(100L, 200L))
                .thenReturn(Optional.of(fileEntity));

        // Act
        FileDownloadDTO result = fileService.getAudioFile(100L, 200L, "mp3");

        // Assert
        assertNotNull(result);
        assertEquals("test.mp3", result.getFileName());
        assertNotNull(result.getFile());
        verify(fileRepository, never()).findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(any(), any(), anyLong());
    }

    @Test
    void shouldReturn404WhenNoOriginalOrConvertedFileExists() {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(100L, 200L))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> fileService.getAudioFile(100L, 200L, "mp3"));

        assertTrue(exception.getMessage().contains("No file available"));
    }

    @Test
    void shouldConvertFileIfFormatDoesNotExist() throws Exception {
        // Arrange
        File convertedFile = Files.createTempFile("converted", ".wav").toFile();

        when(fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(100L, 200L))
                .thenReturn(Optional.of(fileEntity));

        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(
                eq(100L), eq(200L), eq("wav"), anyLong())).thenReturn(Optional.empty());

        when(fileRepository.findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(
                eq(100L), eq(200L), anyLong())).thenReturn(Optional.of(fileEntity));

        when(ffmpegWrapper.convertAudio(any(), eq("wav"))).thenReturn(convertedFile);

        // Act
        FileDownloadDTO result = fileService.getAudioFile(100L, 200L, "wav");

        // Assert
        assertNotNull(result);
        assertEquals("test.mp3", result.getFileName());
        verify(fileRepository).save(any());
    }

    @Test
    void shouldHandleConversionFailureGracefully() throws Exception {
        // Arrange
        when(fileRepository.findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(100L, 200L))
                .thenReturn(Optional.of(fileEntity));

        when(fileRepository.findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(
                eq(100L), eq(200L), eq("wav"), anyLong())).thenReturn(Optional.empty());

        when(fileRepository.findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(
                eq(100L), eq(200L), anyLong())).thenReturn(Optional.of(fileEntity));

        when(ffmpegWrapper.convertAudio(any(), eq("wav")))
                .thenThrow(new RuntimeException("Conversion failed"));

        // Act + Assert
        StorageException exception = assertThrows(StorageException.class,
                () -> fileService.getAudioFile(100L, 200L, "wav"));

        assertTrue(exception.getMessage().contains("Audio conversion failed"));
    }

    @Test
    void shouldReturn400WhenFormatNotSupported() {
        // Act + Assert
        UnsupportedFileFormatException exception = assertThrows(UnsupportedFileFormatException.class,
                () -> fileService.getAudioFile(100L, 200L, "xyz"));

        assertTrue(exception.getMessage().contains("Unsupported format"));
    }
}
