package com.ntros.controller;

import com.ntros.dataservice.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/accounts")
public class AccountController extends AbstractApiController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<?>> getAllAccounts() {
        return accountService.getAllAccounts()
                .handleAsync(this::handleResponseAsync);
    }

}
