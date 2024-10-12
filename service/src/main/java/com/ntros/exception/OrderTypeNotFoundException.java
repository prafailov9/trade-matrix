package com.ntros.exception;

public class OrderTypeNotFoundException extends RuntimeException {

    public OrderTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderTypeNotFoundException(String message) {
        super(message);
    }


}
