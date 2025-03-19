package org.example.audioservice.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends BaseException {

    public StorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
