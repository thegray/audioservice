package org.example.audioservice.util;

import org.example.audioservice.exception.StorageException;
import org.example.audioservice.exception.UnsupportedFileFormatException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public class FileUtils {

    // Private constructor to prevent instantiation
    private FileUtils() {}

    private static final Map<String, String> MIME_TYPE_TO_EXTENSION = new HashMap<>();
    private static final Map<String, String> EXTENSION_TO_MIME_TYPE = new HashMap<>();

    static {
        MIME_TYPE_TO_EXTENSION.put("audio/mpeg", "mp3");
        MIME_TYPE_TO_EXTENSION.put("audio/wav", "wav");
        MIME_TYPE_TO_EXTENSION.put("audio/wave", "wav");
        MIME_TYPE_TO_EXTENSION.put("audio/x-wav", "wav");
        MIME_TYPE_TO_EXTENSION.put("audio/ogg", "ogg");
        MIME_TYPE_TO_EXTENSION.put("audio/aac", "aac");
        MIME_TYPE_TO_EXTENSION.put("audio/flac", "flac");
        MIME_TYPE_TO_EXTENSION.put("audio/opus", "opus");

        // init reverse mapping for extensions
        MIME_TYPE_TO_EXTENSION.forEach((mime, ext) -> EXTENSION_TO_MIME_TYPE.put(ext, mime));
    }

    // for MIME type to file extension conversion
    public static String getExtensionFromMimeType(String mimeType) {
        return MIME_TYPE_TO_EXTENSION.getOrDefault(mimeType, null);
    }

    // for file extension to MIME type conversion
    public static String getMimeTypeFromExtension(String extension) {
        return EXTENSION_TO_MIME_TYPE.getOrDefault(extension, null);
    }

    public static String validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Uploaded File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MIME_TYPE_TO_EXTENSION.containsKey(contentType)) {
            throw new UnsupportedFileFormatException("Unsupported audio file format: " + contentType);
        }
        return MIME_TYPE_TO_EXTENSION.get(contentType);
    }

    public static MediaType getAudioMediaType(String format) {
        return switch (format.toLowerCase()) {
            case "mp3" -> MediaType.valueOf("audio/mpeg");
            case "wav" -> MediaType.valueOf("audio/wav");
            case "ogg" -> MediaType.valueOf("audio/ogg");
            case "aac" -> MediaType.valueOf("audio/aac");
            case "flac" -> MediaType.valueOf("audio/flac");
            case "opus" -> MediaType.valueOf("audio/opus");
            default -> throw new IllegalArgumentException("Unsupported audio format: " + format);
        };
    }
}
