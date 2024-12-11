package com.ntros.exception;

public class RetryLimitExceededException extends RuntimeException {

    public RetryLimitExceededException(String message) {
        super(message);
    }

    public RetryLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

}
