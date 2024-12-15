package com.ntros.exception;

public class TransferFundsAndAssetsException extends RuntimeException {
    public TransferFundsAndAssetsException(String message) {
        super(message);
    }

    public TransferFundsAndAssetsException(String message, Throwable cause) {
        super(message, cause);
    }
}
