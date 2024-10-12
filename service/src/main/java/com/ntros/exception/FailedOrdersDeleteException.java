package com.ntros.exception;

public class FailedOrdersDeleteException extends RuntimeException {
    public FailedOrdersDeleteException(String message) {
        super(message);
    }
}
