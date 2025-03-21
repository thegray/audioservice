package org.example.audioservice.exception.handler;

import org.example.audioservice.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.example.audioservice.payload.ErrorResponse;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .field("user_id")
                                .message(ex.getMessage())
                                .build()
                ))
                .message("User not found")
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(PhraseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePhraseNotFoundException(PhraseNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .field("phrase_id")
                                .message(ex.getMessage())
                                .build()
                ))
                .message("Phrase not found")
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .message(ex.getMessage())
                                .build()
                ))
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .message(ex.getMessage())
                                .build()
                ))
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }


    @ExceptionHandler(UnsupportedFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedFileFormatException(UnsupportedFileFormatException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .message(ex.getMessage())
                                .build()
                ))
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .field("file")
                                .message("File size exceeds the limit of " + maxFileSize)
                                .build()
                ))
                .message("Maximum upload size exceeded")
                .build();

        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessResourceUsageExceptionException(InvalidDataAccessResourceUsageException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .message("Invalid data access")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEndpointException(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .message("Unknown method: "+ ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // for other unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ErrorResponse.ErrorDetail.builder()
                                .message(ex.getMessage())
                                .build()
                ))
                .message("An unexpected error occurred")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
