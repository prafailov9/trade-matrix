package com.ntros.exception;

public class OrderConstraintViolationException extends RuntimeException {

    public OrderConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderConstraintViolationException(String message) {
        super(message);
    }

}
