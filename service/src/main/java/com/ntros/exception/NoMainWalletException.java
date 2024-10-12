package com.ntros.exception;

public class NoMainWalletException extends RuntimeException {

    public NoMainWalletException(String code, String an) {
        super(String.format("No main wallet found for [%s, %s]", code, an));
    }
}
