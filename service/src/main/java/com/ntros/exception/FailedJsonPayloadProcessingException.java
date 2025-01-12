package com.ntros.exception;

public class FailedJsonPayloadProcessingException extends RuntimeException {

    public FailedJsonPayloadProcessingException(String message) {
        super(message);
    }

    public FailedJsonPayloadProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
