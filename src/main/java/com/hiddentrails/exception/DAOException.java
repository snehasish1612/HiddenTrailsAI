package com.hiddentrails.exception;

/** Wraps SQLException from the DAO layer for clean error propagation. */
public class DAOException extends RuntimeException {
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}