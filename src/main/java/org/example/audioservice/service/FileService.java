package org.example.audioservice.service;

import org.example.audioservice.dto.*;
import org.example.audioservice.exception.PhraseNotFoundException;
import org.example.audioservice.exception.StorageException;
import org.example.audioservice.exception.UserNotFoundException;
import org.example.audioservice.library.FFmpegWrapper;
import org.example.audioservice.model.FileEntity;
import org.example.audioservice.repository.FileRepository;
import org.example.audioservice.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    private String baseUploadDir;

    private final FileRepository fileRepository;
    private final FFmpegWrapper ffmpegWrapper;

    public FileService(FileRepository fileRepository, FFmpegWrapper ffmpegWrapper) {
        this.fileRepository = fileRepository;
        this.ffmpegWrapper = ffmpegWrapper;
    }

    private static final Logger Log = LoggerFactory.getLogger(FileService.class);

    public FileDTO saveAudioFile(MultipartFile file, Long userId, Long phraseId) {
        if (userId == 999) { // test UserNotFoundException manually
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }

        if (phraseId == 999) { // test PhraseNotFoundException manually
            throw new PhraseNotFoundException(String.format("Phrase with id %d not found", phraseId));
        }

        String audioFileExt = FileUtils.validateAudioFile(file);

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
            String storedFileName = String.format("%d_%d_%d_%s", userId, phraseId, time, file.getOriginalFilename());
            Path filePath = uploadPath.resolve(storedFileName);

            // Save file to disk
            file.transferTo(filePath.toFile());
            Log.info("transferTo|filePath:{}", filePath.toString());

            FileEntity fileEntity = FileEntity.builder()
                    .userId(userId)
                    .phraseId(phraseId)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .format(audioFileExt)
                    .original(true)
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

    public FileDownloadDTO getAudioFile(Long userId, Long phraseId, String format) {
        Log.info("getAudioFile|userId={}, phraseId={}, format={}", userId, phraseId, format);

        // get possibly file with format
        Optional<FileEntity> existingFile = fileRepository.findTopByUserIdAndPhraseIdAndFormatOrderByCreatedAtDesc(userId, phraseId, format);

        if (existingFile.isPresent()) {
            Path filePath = Paths.get(existingFile.get().getFilePath());
            if (Files.exists(filePath)) {
                Log.info("getAudioFile|Serving existing file at path={}", filePath);
                return FileDownloadDTO.builder()
                        .fileId(existingFile.get().getId())
                        .fileName(existingFile.get().getFileName())
                        .filePath(existingFile.get().getFilePath())
                        .file(readFile(filePath))
                        .build();
            }
        }

        // if not found, try to get the original file
        Optional<FileEntity> originalFile = fileRepository.findTopByUserIdAndPhraseIdAndOriginalOrderByCreatedAtDesc(userId, phraseId, true);

        if (originalFile.isPresent()) {
            String originalFileName = originalFile.get().getFileName();
            Path originalFilePath = Paths.get(originalFile.get().getFilePath());
            if (Files.exists(originalFilePath)) {
                Log.info("getAudioFile|Original file found at path={}", originalFilePath);

                // convert and save the file with the format
                Path convertedFilePath = convertAudio(originalFilePath.toFile(), format);

                Log.info("saveConvertedFile|Saving file={} at path={}", originalFileName, convertedFilePath);

                FileEntity convertedEntity = FileEntity.builder()
                        .userId(userId)
                        .phraseId(phraseId)
                        .fileName(originalFileName)
                        .filePath(convertedFilePath.toString())
                        .format(format)
                        .createdAt(System.currentTimeMillis())
                        .build();

                fileRepository.save(convertedEntity);

                return FileDownloadDTO.builder()
                        .fileName(convertedEntity.getFileName())
                        .filePath(convertedEntity.getFilePath())
                        .file(readFile(convertedFilePath))
                        .build();
            } else {
                throw new StorageException("Original file not found at: " + originalFilePath);
            }
        }

        throw new StorageException("No file available for userId=" + userId + ", phraseId=" + phraseId);
    }

    private Path convertAudio(File rawFile, String format) {
        Log.info("convertAudio|Converting file to format={}", format);

        File convertedFile = ffmpegWrapper.convertAudio(rawFile, format);
        if (convertedFile != null) {
            Log.info("convertAudio|Converted file saved at path={}", convertedFile.getAbsolutePath());
            return convertedFile.toPath();
        } else {
            throw new StorageException("Conversion failed for format: " + format);
        }
    }

    private byte[] readFile(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            Log.error("readFile|Failed to read file: {}", filePath, e);
            throw new StorageException("Failed to read file: " + e.getMessage());
        }
    }
}
