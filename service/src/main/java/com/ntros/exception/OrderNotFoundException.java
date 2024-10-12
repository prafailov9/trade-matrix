package com.ntros.exception;

public class OrderNotFoundException extends RuntimeException {


    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

}
