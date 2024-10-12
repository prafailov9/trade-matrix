package com.ntros.exception;

public class ExchangeRateNotFoundForPairException extends RuntimeException {

    public ExchangeRateNotFoundForPairException(String from, String to) {
        super(String.format("Exchange rate not found for: %s/%s", from, to));
    }

}
