package com.ntros.exception;

public class WalletCreateFailedException extends RuntimeException {

    public WalletCreateFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
