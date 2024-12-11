package com.ntros.controller;

import com.ntros.converter.AccountConverter;
import com.ntros.dataservice.account.AccountService;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("accounts")
public class AccountController extends AbstractApiController {

    private final AccountService accountService;
    private final AccountConverter accountConverter;

    @Autowired
    public AccountController(AccountService accountService, AccountConverter accountConverter) {
        this.accountService = accountService;
        this.accountConverter = accountConverter;
    }

    @GetMapping("/{accountNumber}")
    public CompletableFuture<ResponseEntity<?>> getAccountByAccountNumber(
            @PathVariable("accountNumber")
            @Pattern(regexp = "\\d+", message = "ACCOUNT_NUMBER must be a number.")
            String accountNumber) {
        return accountService.getAccountByAccountNumber(accountNumber)
                .thenApplyAsync(accountConverter::toDTO)
                .handleAsync(this::handleResponseAsync);
    }

    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<?>> getAllAccounts() {
        return accountService.getAllAccounts()
                .thenApplyAsync(accounts ->
                        accounts.stream()
                                .map(accountConverter::toDTO)
                                .collect(Collectors.toList()))
                .handleAsync(this::handleResponseAsync);
    }

}
