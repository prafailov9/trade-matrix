package com.ntros.exception;

public class PositionNotFoundException extends RuntimeException {

    public PositionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PositionNotFoundException(String message) {
        super(message);
    }

}
