package org.example.audioservice.controller;

import org.example.audioservice.dto.FileDTO;
import org.example.audioservice.exception.PhraseNotFoundException;
import org.example.audioservice.exception.UserNotFoundException;
import org.example.audioservice.payload.ApiResponse;
import org.example.audioservice.payload.FileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/audio")
public class FileController {

    private static final Logger Log = LoggerFactory.getLogger(FileController.class);

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


//        FileDTO fileDTO = audioService.saveAudio(file, userId, phraseId);

        //mock data
        long id = System.currentTimeMillis();
        String name = id + ".mp3";
        FileDTO fileDTO = FileDTO.builder()
                .fileId(id)
                .fileName(name)
                .filePath("/storage/audio/" + name)
                .build();

        FileResponse fileResponse = FileResponse.from(fileDTO);
        ApiResponse<FileResponse> response = ApiResponse.<FileResponse>builder()
                .status("success")
                .data(fileResponse)
                .message("Audio uploaded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/phrase/{phraseId}/{audioFormat}")
    public ResponseEntity<ApiResponse<String>> getAudioFile(@PathVariable String userId, @PathVariable String phraseId, @PathVariable String audioFormat) {
        Log.info("getAudioFile|userId:{}, phraseId:{}, audioFormat:{}", userId, phraseId, audioFormat);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("success")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

}
