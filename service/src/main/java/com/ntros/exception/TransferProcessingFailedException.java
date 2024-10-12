package com.ntros.exception;

public class TransferProcessingFailedException extends RuntimeException {

    public TransferProcessingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
