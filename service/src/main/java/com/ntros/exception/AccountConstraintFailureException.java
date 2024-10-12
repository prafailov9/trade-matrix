package com.ntros.exception;


import com.ntros.model.account.Account;

public class AccountConstraintFailureException extends RuntimeException {

    public AccountConstraintFailureException(Account acc) {
        super(String.format("Failed to persist Account: %s", acc));
    }

}
