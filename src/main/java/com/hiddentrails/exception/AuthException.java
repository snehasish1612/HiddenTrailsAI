package com.hiddentrails.exception;

/** Thrown by AuthService for invalid credentials or token issues. */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}