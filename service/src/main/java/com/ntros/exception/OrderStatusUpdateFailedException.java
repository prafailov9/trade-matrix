package com.ntros.exception;

public class OrderStatusUpdateFailedException extends RuntimeException {

    public OrderStatusUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderStatusUpdateFailedException(String message) {
        super(message);
    }

}
