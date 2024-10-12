package com.ntros.exception;

public class CurrencyInactiveStateException extends RuntimeException {

    public CurrencyInactiveStateException(String code) {
        super(String.format("Currency %s is inactive", code));
    }

}
