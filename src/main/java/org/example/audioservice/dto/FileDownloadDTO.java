package org.example.audioservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FileDownloadDTO extends FileDTO {
    private byte[] file;
}