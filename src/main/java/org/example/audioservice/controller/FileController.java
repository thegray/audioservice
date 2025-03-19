package org.example.audioservice.controller;

import org.example.audioservice.dto.FileDTO;
import org.example.audioservice.exception.PhraseNotFoundException;
import org.example.audioservice.exception.UserNotFoundException;
import org.example.audioservice.payload.ApiResponse;
import org.example.audioservice.payload.FileResponse;
import org.example.audioservice.service.FileService;
import org.example.audioservice.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/audio")
public class FileController {

    private final FileService fileService;
    private static final Logger Log = LoggerFactory.getLogger(FileController.class);

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/user/{userId}/phrase/{phraseId}")
    public ResponseEntity<ApiResponse<FileResponse>> uploadAudioFile(
            @PathVariable Long userId,
            @PathVariable Long phraseId,
            @RequestParam("file") MultipartFile file) {

        Log.info("uploadAudioFile|userId:{},phraseId:{}", userId, phraseId);

        if (userId == 999) { // test UserNotFoundException manually
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }

        if (phraseId == 999) { // test PhraseNotFoundException manually
            throw new PhraseNotFoundException(String.format("Phrase with id %d not found", phraseId));
        }

        FileDTO fileDTO = fileService.saveAudioFile(file, userId, phraseId);

        FileResponse fileResponse = FileResponse.from(fileDTO);
        ApiResponse<FileResponse> response = ApiResponse.<FileResponse>builder()
                .status("success")
                .data(fileResponse)
                .message("Audio uploaded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/phrase/{phraseId}/{audioFormat}")
    public ResponseEntity<byte[]> getAudioFile(@PathVariable Long userId, @PathVariable Long phraseId, @PathVariable String audioFormat) throws IOException {
        Log.info("getAudioFile|userId:{}, phraseId:{}, audioFormat:{}", userId, phraseId, audioFormat);

        // Validate user and phrase, similar to upload, maybe can create function

        byte[] fileData = fileService.getAudioFile(userId, phraseId, audioFormat);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(FileUtils.getAudioMediaType(audioFormat));
        headers.setContentLength(fileData.length);
        headers.setContentDispositionFormData("attachment", phraseId + "." + audioFormat);

        Log.info("getAudioFile|fileDataSize:{}", fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

}
