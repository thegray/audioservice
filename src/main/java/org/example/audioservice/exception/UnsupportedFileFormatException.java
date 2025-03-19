package org.example.audioservice.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedFileFormatException extends BaseException {

    public UnsupportedFileFormatException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
