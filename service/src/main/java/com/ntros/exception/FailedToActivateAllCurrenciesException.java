package com.ntros.exception;

public class FailedToActivateAllCurrenciesException extends RuntimeException {

    public FailedToActivateAllCurrenciesException(String message, Throwable cause) {
        super(String.format("Failed to activate all currencies: %s", message), cause);
    }

}
