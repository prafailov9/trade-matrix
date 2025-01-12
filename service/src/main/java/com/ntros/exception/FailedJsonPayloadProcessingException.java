package com.ntros.exception;

public class FailedJsonPayloadProcessingException extends RuntimeException {

    private FailedJsonPayloadProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    private FailedJsonPayloadProcessingException(String message) {
        super(message);
    }

    public static FailedJsonPayloadProcessingException with(String message, Throwable cause) {
        return new FailedJsonPayloadProcessingException(message, cause);
    }

    public static FailedJsonPayloadProcessingException with(String message) {
        return new FailedJsonPayloadProcessingException(message);
    }

}
