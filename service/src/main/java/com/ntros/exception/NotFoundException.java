package com.ntros.exception;

public class NotFoundException extends RuntimeException {

    private NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException with(String message, Throwable cause) {
        return new NotFoundException(message, cause);
    }

    public static NotFoundException with(String message) {
        return new NotFoundException(message);
    }

}
