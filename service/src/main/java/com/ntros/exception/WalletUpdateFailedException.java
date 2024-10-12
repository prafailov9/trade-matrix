package com.ntros.exception;

public class WalletUpdateFailedException extends RuntimeException {

    public WalletUpdateFailedException(String message) {
        super(message);
    }
}
