package com.ntros.exception;

public class InvalidCacheStateException extends RuntimeException {

    public InvalidCacheStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCacheStateException(String message) {
        super(message);
    }

    public static InvalidCacheStateException with(String message, Throwable cause) {
        return new InvalidCacheStateException(message, cause);
    }

    public static InvalidCacheStateException with(String message) {
        return new InvalidCacheStateException(message);
    }


}
