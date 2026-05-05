package com.hiddentrails.exception;

/** Thrown by AIService when the external AI API call fails. */
public class AIServiceException extends RuntimeException {
    public AIServiceException(String message) {
        super(message);
    }
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}