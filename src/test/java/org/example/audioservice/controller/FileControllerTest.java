package org.example.audioservice.controller;

import org.example.audioservice.dto.FileDTO;
import org.example.audioservice.dto.FileDownloadDTO;
import org.example.audioservice.exception.PhraseNotFoundException;
import org.example.audioservice.exception.ResourceNotFoundException;
import org.example.audioservice.exception.UserNotFoundException;
import org.example.audioservice.exception.handler.GlobalExceptionHandler;
import org.example.audioservice.payload.ApiResponse;
import org.example.audioservice.service.FileService;
import org.example.audioservice.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private FileDownloadDTO fileDownloadDTO;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        fileDownloadDTO = FileDownloadDTO.builder()
                .fileName("test-audio.mp3")
                .file(new byte[]{1, 2, 3, 4})
                .build();
    }

    // test for successful file upload
    @Test
    void shouldUploadAudioFileSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "test data".getBytes(StandardCharsets.UTF_8)
        );

        FileDTO fileDTO = FileDTO.builder()
                .fileId(1L)
                .fileName("test.mp3")
                .filePath("/path/to/file")
                .build();

        when(fileService.saveAudioFile(any(), eq(100L), eq(200L))).thenReturn(fileDTO);

        // Act & Assert
        mockMvc.perform(multipart("/v1/audio/user/100/phrase/200")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fileId").value(1L))
                .andExpect(jsonPath("$.data.fileName").value("test.mp3"))
                .andExpect(jsonPath("$.data.filePath").value("/path/to/file"));

        // Verify
        verify(fileService).saveAudioFile(any(), eq(100L), eq(200L));
    }

    // test for successful file download
    @Test
    void shouldDownloadFileSuccessfully() throws Exception {
        // Mock service call
        when(fileService.getAudioFile(1L, 2L, "mp3")).thenReturn(fileDownloadDTO);

        mockMvc.perform(get("/v1/audio/user/1/phrase/2/mp3"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/mpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"test-audio.mp3\""))
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4}));
    }

    // test for unsupported file format
    @Test
    void shouldReturn400ForUnsupportedFormatOnDownload() throws Exception {
        // Arrange
        when(fileService.getAudioFile(eq(100L), eq(200L), eq("xyz")))
                .thenThrow(new IllegalArgumentException("Unsupported format"));

        // Act & Assert
        mockMvc.perform(get("/v1/audio/user/100/phrase/200/xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Unsupported format"));

        // Verify
        verify(fileService).getAudioFile(eq(100L), eq(200L), eq("xyz"));
    }

    // test for file not found on download
    @Test
    void shouldReturn404WhenFileNotFoundOnDownload() throws Exception {
        // Arrange
        when(fileService.getAudioFile(eq(100L), eq(200L), eq("mp3")))
                .thenThrow(new ResourceNotFoundException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/v1/audio/user/100/phrase/200/mp3"))
                .andExpect(status().isNotFound()) // ✅ Expect 404 status
                .andExpect(jsonPath("$.message").value("File not found"));

        // Verify
        verify(fileService).getAudioFile(eq(100L), eq(200L), eq("mp3"));
    }
}
