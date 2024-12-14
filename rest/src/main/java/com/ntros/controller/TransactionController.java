package com.ntros.controller;

import com.ntros.converter.TransactionConverter;
import com.ntros.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/tx")
public class TransactionController extends AbstractApiController {

    private final TransactionService transactionService;
    private final TransactionConverter transactionConverter;

    @Autowired
    public TransactionController(TransactionService transactionService, TransactionConverter transactionConverter) {
        this.transactionService = transactionService;
        this.transactionConverter = transactionConverter;
    }

    @GetMapping("/all")
    CompletableFuture<ResponseEntity<?>> getAll() {
        return transactionService.getAllTransactions()
                .thenApplyAsync(transactions ->
                        transactions.stream()
                                .map(transactionConverter::toDTO)
                                .collect(Collectors.toList()))
                .handleAsync(this::handleResponseAsync);
    }

}
