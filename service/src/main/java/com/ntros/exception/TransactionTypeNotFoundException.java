package com.ntros.exception;

public class TransactionTypeNotFoundException extends RuntimeException {
    public TransactionTypeNotFoundException(String message) {
        super(message);
    }


    public TransactionTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
