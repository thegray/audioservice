package org.example.audioservice.controller;

import org.example.audioservice.dto.FileDownloadDTO;
import org.example.audioservice.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @Test
    void shouldReturnFileSuccessfully() throws Exception {
        // Arrange
        FileDownloadDTO fileDownloadDTO = FileDownloadDTO.builder()
                .fileName("test.mp3")
                .file(new byte[]{1, 2, 3, 4})
                .build();

        when(fileService.getAudioFile(100L, 200L, "mp3")).thenReturn(fileDownloadDTO);

        // Act
        ResponseEntity<byte[]> response = fileController.getAudioFile(100L, 200L, "mp3");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, response.getBody());
        assertEquals("attachment; filename=\"test.mp3\"", response.getHeaders().getContentDisposition().toString());
    }

    @Test
    void shouldReturn404WhenFileNotFound() throws Exception {
        // Arrange
        when(fileService.getAudioFile(100L, 200L, "mp3"))
                .thenThrow(new RuntimeException("File not found"));

        // Act + Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileController.getAudioFile(100L, 200L, "mp3");
        });

        assertEquals("File not found", exception.getMessage());
    }
}
