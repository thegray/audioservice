package org.example.audioservice.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FileDTO {
    private Long fileId;
    private String fileName;
    private String filePath;
}
