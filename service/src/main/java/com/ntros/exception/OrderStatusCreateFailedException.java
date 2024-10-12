package com.ntros.exception;

public class OrderStatusCreateFailedException extends RuntimeException {

    public OrderStatusCreateFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderStatusCreateFailedException(String message) {
        super(message);
    }

}
