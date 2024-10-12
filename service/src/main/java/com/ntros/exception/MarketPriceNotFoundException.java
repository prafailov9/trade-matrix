package com.ntros.exception;

public class MarketPriceNotFoundException extends RuntimeException {
    public MarketPriceNotFoundException(String message) {
        super(message);
    }
}
