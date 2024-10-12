package com.ntros.exception;

public class CreateOrderFailedException extends RuntimeException {
    public CreateOrderFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateOrderFailedException(String message) {
        super(message);
    }
}
