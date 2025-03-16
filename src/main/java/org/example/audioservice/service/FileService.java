package org.example.audioservice.service;

import org.example.audioservice.controller.FileController;
import org.example.audioservice.dto.FileDTO;
import org.example.audioservice.exception.StorageException;
import org.example.audioservice.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    private String baseUploadDir;

    private static final Logger Log = LoggerFactory.getLogger(FileService.class);

    public FileDTO saveAudioFile(MultipartFile file, Long userId, Long phraseId) {
        FileUtils.validateAudioFile(file);

        try {
            // Create date-based directory
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path uploadPath = Paths.get(baseUploadDir, dateFolder);
            Log.info("settings|uploadPath:{}", uploadPath);

            // Ensure the upload directory exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                Log.info("createDirectories|uploadPath:{}", uploadPath);
            }

            // Generate a unique file name based on user, phrase ID, and time
            Long time = System.currentTimeMillis();
            String fileName = String.format("%d_%d_%d_%s", userId, phraseId, time, file.getOriginalFilename());
//            String fileName = userId + "_" + phraseId + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file to disk
            file.transferTo(filePath.toFile());
            Log.info("transferTo|filePath:{}", filePath.toString());

            // Build DTO with metadata
            return FileDTO.builder()
                    .fileId(System.currentTimeMillis()) // Mock ID for now
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .build();

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + e.getMessage());
        }
    }
}
