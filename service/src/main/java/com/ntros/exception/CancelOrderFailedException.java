package com.ntros.exception;

public class CancelOrderFailedException extends RuntimeException {
    public CancelOrderFailedException(String message) {
        super(message);
    }

    public CancelOrderFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
