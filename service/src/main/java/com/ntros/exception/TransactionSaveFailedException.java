package com.ntros.exception;

public class TransactionSaveFailedException extends RuntimeException {
    public TransactionSaveFailedException(String message) {
        super(message);
    }
  public TransactionSaveFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
