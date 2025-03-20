package org.example.audioservice.service;

import org.example.audioservice.dto.*;
import org.example.audioservice.exception.PhraseNotFoundException;
import org.example.audioservice.exception.StorageException;
import org.example.audioservice.exception.UnsupportedFileFormatException;
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
                    .groupId(time)
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
        Log.info("get_audio_file|userId={}, phraseId={}, format={}", userId, phraseId, format);

        // get latest file for the phrase
        Optional<FileEntity> latestFile = fileRepository
                .findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(userId, phraseId);

        if (latestFile.isEmpty()) {
            Log.info("get_audio_file|no available file, userId={}, phraseId={}", userId, phraseId);
            throw new StorageException("No file available for userId=" + userId + ", phraseId=" + phraseId);
        }

        long groupId = latestFile.get().getGroupId();

        FileEntity resultFileEntity = null;

        if (latestFile.get().getFormat().equalsIgnoreCase(format)) {
            Path filePath = Paths.get(latestFile.get().getFilePath());

            if (Files.exists(filePath)) {
                resultFileEntity = latestFile.get();
            } else {
                throw new StorageException("File missing on disk: " + filePath.toString());
            }
        }

        if (resultFileEntity == null) {
            Optional<FileEntity> existingFile = fileRepository
                    .findTopByUserIdAndPhraseIdAndFormatAndGroupIdOrderByCreatedAtDesc(
                            userId, phraseId, format.toLowerCase(), groupId);

            if (existingFile.isPresent()) {
                Path filePath = Paths.get(existingFile.get().getFilePath());

                if (Files.exists(filePath)) {
                    Log.info("get_audio_file|serving existing file at path={}", filePath);
                    resultFileEntity = existingFile.get();
                } else {
                    // file is missing on storage
                    Log.info("get_audio_file|missing existing file at path={}", filePath);
                    throw new StorageException("Missing file: " + filePath.toString());
                }
            }

            Log.info("get_audio_file|no existing file for group={}, format={}", groupId, format);

            Optional<FileEntity> originalFile = fileRepository
                    .findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(
                            userId, phraseId, groupId);

            if (originalFile.isEmpty()) {
                throw new StorageException("No original file found for groupId=" + groupId);
            }

            // convert from the latest original file when file with format not exist
            Path convertedFilePath = convertAudio(Paths.get(originalFile.get().getFilePath()).toFile(), format);

            resultFileEntity = FileEntity.builder()
                    .userId(userId)
                    .phraseId(phraseId)
                    .fileName(originalFile.get().getFileName()) // use the same filename
                    .filePath(convertedFilePath.toString())
                    .format(format)
                    .groupId(groupId) // use the same groupId as original
                    .createdAt(System.currentTimeMillis())
                    .build();

            fileRepository.save(resultFileEntity);
        }

        // return converted file to user
        Path filePath = Paths.get(resultFileEntity.getFilePath());
        return FileDownloadDTO.builder()
                .fileId(resultFileEntity.getId())
                .fileName(resultFileEntity.getFileName())
                .filePath(resultFileEntity.getFilePath())
                .file(readFile(filePath))
                .build();
    }

    private Path convertAudio(File rawFile, String format) {
        Log.info("convert_audio_process|converting file to format={}", format);

        File convertedFile = ffmpegWrapper.convertAudio(rawFile, format);
        if (convertedFile != null) {
            Log.info("convert_audio_process|converted file saved at path={}", convertedFile.getAbsolutePath());
            return convertedFile.toPath();
        } else {
            Log.info("convert_audio_process|convert file attempt failed");
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
