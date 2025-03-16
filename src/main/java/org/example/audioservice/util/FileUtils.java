package org.example.audioservice.util;

import org.example.audioservice.exception.StorageException;
import org.example.audioservice.exception.UnsupportedFileFormatException;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    // Private constructor to prevent instantiation
    private FileUtils() {}

    public static void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Uploaded File is empty");
        }

        // Validate file type (allow only audio files)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new UnsupportedFileFormatException("Unsupported audio file format: " + contentType);
        }
    }
}
