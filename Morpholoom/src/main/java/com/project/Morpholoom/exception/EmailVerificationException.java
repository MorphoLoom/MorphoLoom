package com.project.Morpholoom.exception;

import lombok.Getter;

@Getter
public class EmailVerificationException extends RuntimeException {
    
    private final String errorCode;
    
    public EmailVerificationException(String message) {
        super(message);
        this.errorCode = "EMAIL_VERIFICATION_FAILED";
    }
    
    public EmailVerificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
