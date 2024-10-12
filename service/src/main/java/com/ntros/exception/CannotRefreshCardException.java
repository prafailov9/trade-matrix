package com.ntros.exception;

public class CannotRefreshCardException extends RuntimeException {

    public CannotRefreshCardException(String message) {
        super(message);
    }

    public CannotRefreshCardException(String message, Throwable cause) {
        super(message, cause);
    }

}
