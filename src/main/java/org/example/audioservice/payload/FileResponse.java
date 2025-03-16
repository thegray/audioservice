package org.example.audioservice.payload;

import lombok.Builder;
import lombok.Getter;
import org.example.audioservice.dto.FileDTO;

@Getter
@Builder
public class FileResponse {
    private Long fileId;
    private String fileName;
    private String filePath;

    public static FileResponse from(FileDTO dto) {
        return FileResponse.builder()
                .fileId(dto.getFileId())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .build();
    }
}
