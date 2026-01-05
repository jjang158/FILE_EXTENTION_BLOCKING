package com.example.extensionblocker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E001", "Invalid request parameters"),
    POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "E002", "Policy violation"),

    // 409 Conflict
    ALREADY_EXISTS(HttpStatus.CONFLICT, "E003", "Resource already exists"),

    // 500 Internal Server Error
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "Internal server error");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
