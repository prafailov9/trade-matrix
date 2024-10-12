package com.ntros.exception;

public class WalletNotFoundForANException extends RuntimeException {

    public WalletNotFoundForANException(String currencyCode, String an) {
        super(String.format("%s wallet not found for account number: %s", currencyCode, an));
    }
}
