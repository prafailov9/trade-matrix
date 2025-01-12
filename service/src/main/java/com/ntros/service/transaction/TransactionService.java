package com.ntros.service.transaction;

import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {
    TransactionType getTransactionType(String type);
    Transaction createTransaction(Transaction transaction);
    CompletableFuture<List<Transaction>> getAllTransactionsAsync();
    CompletableFuture<List<Transaction>> getAllTransactionsByPortfolio(String portfolioName);
    CompletableFuture<List<Transaction>> getAllTransactionsByAccountNumber(String accountNumber);
}
