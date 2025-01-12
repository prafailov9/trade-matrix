package com.ntros.exception;

public class DataConstraintFailureException extends RuntimeException {


    private DataConstraintFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    private DataConstraintFailureException(String message) {
        super(message);
    }

    public static DataConstraintFailureException with(String message, Throwable cause) {
        return new DataConstraintFailureException(message, cause);
    }

    public static DataConstraintFailureException with(String message) {
        return new DataConstraintFailureException(message);
    }

}
