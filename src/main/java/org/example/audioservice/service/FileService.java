package org.example.audioservice.service;

import org.example.audioservice.dto.*;
import org.example.audioservice.exception.*;
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
        Log.info("save_audio_file|start|userId={}, phraseId={}", userId, phraseId);

        // only mock
        if (userId == 999) { // test UserNotFoundException manually
            Log.info("save_audio_file|fail|no userId={}", userId);
            throw new UserNotFoundException(String.format("User with id %d not found", userId));
        }

        if (phraseId == 999) { // test PhraseNotFoundException manually
            Log.info("save_audio_file|fail|no phraseId={}", phraseId);
            throw new PhraseNotFoundException(String.format("Phrase with id %d not found", phraseId));
        }

        String audioFileExt = FileUtils.validateAudioFile(file).toLowerCase();

        Path filePath = null;

        try {
            // Create date-based directory
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path uploadPath = Paths.get(baseUploadDir, dateFolder);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique file name based on user, phrase ID, and time
            Long time = System.currentTimeMillis();
            String storedFileName = String.format("%d_%d_%d_%s", userId, phraseId, time, file.getOriginalFilename());
            filePath = uploadPath.resolve(storedFileName);

            // Save file to disk
            file.transferTo(filePath.toFile());
            Log.info("save_audio_file|success|stored file at path={}", filePath.toString());

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

            Log.info("save_audio_file|end|file saved to db path={}", filePath.toString());
            return FileDTO.builder()
                    .fileId(savedFile.getId()) // should use file_id
                    .fileName(savedFile.getFileName())
                    .filePath(savedFile.getFilePath())
                    .build();

        } catch (IOException e) {
            if (filePath != null) {
                Log.error("save_audio_file|fail|failed to store file={} error={}", filePath.toString(), e.getMessage());
            } else {
                Log.error("save_audio_file|fail|file path was not generated. error={}", e.getMessage());
            }
            throw new StorageException("Failed to store file: " + e.getMessage());
        }
    }

    public FileDownloadDTO getAudioFile(Long userId, Long phraseId, String format) {
        Log.info("get_audio_file|start|userId={}, phraseId={}, format={}", userId, phraseId, format);

        format = format.toLowerCase();

        // file format validation
        if (FileUtils.getMimeTypeFromExtension(format.toLowerCase()) == null) {
            Log.info("get_audio_file|fail|format not supported={}", format);
            throw new UnsupportedFileFormatException("Unsupported format: " + format);
        }

        // get latest file for the phrase
        Optional<FileEntity> latestFile = fileRepository
                .findTopByUserIdAndPhraseIdOrderByCreatedAtDesc(userId, phraseId);

        if (latestFile.isEmpty()) {
            Log.info("get_audio_file|fail|no available file, userId={}, phraseId={}", userId, phraseId);
            throw new ResourceNotFoundException("No file available for userId: " + userId + ", phraseId: " + phraseId + ", format: " + format);
        }

        long groupId = latestFile.get().getGroupId();

        FileEntity resultFileEntity = null;

        if (latestFile.get().getFormat().equalsIgnoreCase(format)) {
            Path filePath = Paths.get(latestFile.get().getFilePath());

            if (Files.exists(filePath)) {
                Log.info("get_audio_file|success|serving latest file at path={}", filePath);
                resultFileEntity = latestFile.get();
            } else {
                Log.info("get_audio_file|fail|missing file at path={}", filePath);
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
                    Log.info("get_audio_file|success|serving existing file at path={}", filePath);
                    resultFileEntity = existingFile.get();
                } else {
                    // file is missing on storage
                    Log.info("get_audio_file|fail|missing file at path={}", filePath);
                    throw new StorageException("Missing file: " + filePath.toString());
                }
            }

            Log.info("get_audio_file|no existing file for group={}, format={}", groupId, format);

            Optional<FileEntity> originalFile = fileRepository
                    .findTopByUserIdAndPhraseIdAndGroupIdOrderByCreatedAtAsc(
                            userId, phraseId, groupId);

            if (originalFile.isEmpty()) {
                Log.info("get_audio_file|fail|no original file for userId={}, phraseId={}, group={}", userId, phraseId, groupId);
                throw new ResourceNotFoundException("No original file available for userId: " + userId + ", phraseId: " + phraseId);
            }

            // convert from the latest original file when file with format not exist
            Path convertedFilePath = convertAudioProcess(originalFile.get().getId(), Paths.get(originalFile.get().getFilePath()).toFile(), format);
            Log.info("get_audio_file|success|converted file id={} userId={}, phraseId={}, group={}",
                    originalFile.get().getId(), userId, phraseId, groupId
            );
            resultFileEntity = FileEntity.builder()
                    .userId(userId)
                    .phraseId(phraseId)
                    .fileName(originalFile.get().getFileName()) // use the same filename
                    .filePath(convertedFilePath.toString())
                    .format(format)
                    .groupId(groupId) // use the same groupId as original
                    .createdAt(System.currentTimeMillis())
                    .build();

            Log.info("get_audio_file|save converted file to db id={} userId={}, phraseId={}, group={}",
                    originalFile.get().getId(), userId, phraseId, groupId
            );
            fileRepository.save(resultFileEntity);
        }

        Log.info("get_audio_file|end|respond with file={}, path={} for userId={}, phraseId={}, format={}",
                resultFileEntity.getFilePath(), resultFileEntity.getFileName(),
                userId, phraseId, format
        );
        // return converted file to user
        Path filePath = Paths.get(resultFileEntity.getFilePath());
        return FileDownloadDTO.builder()
                .fileId(resultFileEntity.getId())
                .fileName(resultFileEntity.getFileName())
                .filePath(resultFileEntity.getFilePath())
                .file(readFile(filePath))
                .build();
    }

    private Path convertAudioProcess(Long id, File rawFile, String format) {
        Log.info("convert_audio_process|start|fileId={}, format={}", id, format);

        File convertedFile = null;
        try {
            convertedFile = ffmpegWrapper.convertAudio(rawFile, format);

            if (convertedFile == null || !convertedFile.exists()) {
                throw new StorageException("File conversion failed, file id: " + id);
            }

            Log.info("convert_audio_process|success|converted file id={} saved at path={}", id, convertedFile.getAbsolutePath());

            return convertedFile.toPath();

        } catch (Exception e) {
            Log.error("convert_audio_process|fail|file conversion failed id={}, format={}, error={}", id, format, e.getMessage(), e);

            // cleanup if file partially created
            if (convertedFile != null && convertedFile.exists()) {
                cleanupFile(convertedFile, id);
            }
            throw new StorageException("Audio conversion failed for format: " + format + ", error" + e.getMessage());
        }
    }

    private byte[] readFile(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            Log.error("read_file|failed to read file: {}", filePath, e);
            throw new StorageException("Failed to read file: " + e.getMessage());
        }
    }

    private void cleanupFile(File file, Long id) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
                Log.warn("cleanup_file|deleted partial file id={} at path={}", id, file.getAbsolutePath());
            } catch (IOException deleteEx) {
                Log.error("cleanup_file|failed to delete partial file id={}, error={}", id, deleteEx.getMessage());
            }
        }
    }

}
