package org.example.audioservice.service;

import org.example.audioservice.dto.FileDTO;
import org.example.audioservice.exception.StorageException;
import org.example.audioservice.model.FileEntity;
import org.example.audioservice.repository.FileRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    private String baseUploadDir;

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

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

            FileEntity fileEntity = FileEntity.builder()
                    .userId(userId)
                    .phraseId(phraseId)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .format(file.getContentType())
                    .createdAt(time)
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);

            return FileDTO.builder()
                    .fileId(savedFile.getId()) // should use file_id
                    .fileName(savedFile.getFileName())
                    .filePath(savedFile.getFilePath())
                    .build();

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + e.getMessage());
        }
    }
}
