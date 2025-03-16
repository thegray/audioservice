package org.example.audioservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDTO {
    private Long fileId;
    private String fileName;
    private String filePath;
}
