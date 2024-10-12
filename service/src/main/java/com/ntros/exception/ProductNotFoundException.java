package com.ntros.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }

}
