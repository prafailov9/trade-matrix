package com.ntros.exception;

public class InvalidArgumentException extends RuntimeException {

    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidArgumentException(String message) {
        super(message);
    }

    public static InvalidArgumentException with(String message, Throwable cause) {
        return new InvalidArgumentException(message, cause);
    }

    public static InvalidArgumentException with(String message) {
        return new InvalidArgumentException(message);
    }

}
