package org.example.audioservice.exception;

import org.springframework.http.HttpStatus;

public class PhraseNotFoundException extends BaseException {

    public PhraseNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
