package com.ntros.exception;

public class DataAccessViolationException extends RuntimeException {

    private DataAccessViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    private DataAccessViolationException(String message) {
        super(message);
    }

    public static DataAccessViolationException with(String message, Throwable cause) {
        return new DataAccessViolationException(message, cause);
    }

    public static DataAccessViolationException with(String message) {
        return new DataAccessViolationException(message);
    }
    
}
