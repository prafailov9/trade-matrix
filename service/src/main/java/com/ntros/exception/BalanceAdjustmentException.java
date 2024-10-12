package com.ntros.exception;

public class BalanceAdjustmentException extends RuntimeException {
  public BalanceAdjustmentException(String message) {
    super(message);
  }

  public BalanceAdjustmentException(String message, Throwable cause) {
    super(message, cause);
  }
}
