package com.ntros.exception;

public class CardNotCreatedException extends RuntimeException {

    public CardNotCreatedException(String message) {
        super(message);
    }

    public CardNotCreatedException(String message, Throwable cause) {
        super(message, cause);
    }

}
