package com.example.Playlist.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_KEY(101, "Invalid key", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED(102, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(103, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(104, "You dont have permission", HttpStatus.FORBIDDEN),
    INVALID_AUDIO_FORMAT(105, "Invalid audio file", HttpStatus.NOT_FOUND),
    NAME_IS_REQUIRED(106, "Name is required", HttpStatus.BAD_REQUEST),
    IS_ACTIVE_IS_REQUIRED(107, "Is active is required", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(108, "File not found", HttpStatus.NOT_FOUND),
    INVALID_GENRE(109, "Genre not found or inactive ",HttpStatus.NOT_FOUND);
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private  HttpStatusCode statusCode;
}
