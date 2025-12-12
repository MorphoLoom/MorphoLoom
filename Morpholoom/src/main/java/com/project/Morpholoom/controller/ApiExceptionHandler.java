package com.project.Morpholoom.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.Morpholoom.exception.EmailVerificationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<Map<String, Object>> handleEmailVerification(EmailVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "success", false,
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage()
            )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "success", false,
                "errorCode", "BAD_REQUEST",
                "message", ex.getMessage()
            )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of(
                "success", false,
                "errorCode", "INTERNAL_ERROR",
                "message", "서버 오류: " + ex.getMessage()
            )
        );
    }
}

