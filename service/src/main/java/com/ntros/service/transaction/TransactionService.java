package com.ntros.service.transaction;

import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {

    CompletableFuture<TransactionType> getTransactionType(String type);

    CompletableFuture<Transaction> createTransaction(Transaction transaction);
    CompletableFuture<List<Transaction>> getAllTransactions();
    CompletableFuture<List<Transaction>> getAllTransactionsByPortfolio(String portfolioName);
    CompletableFuture<List<Transaction>> getAllTransactionsByAccountNumber(String accountNumber);
}
