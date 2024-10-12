package com.ntros.exception;

public class InsufficientAssetsException extends RuntimeException {
    public InsufficientAssetsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientAssetsException(String message) {
        super(message);
    }
}
