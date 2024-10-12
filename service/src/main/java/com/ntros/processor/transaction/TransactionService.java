package com.ntros.processor.transaction;

import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;

import java.util.concurrent.CompletableFuture;

public interface TransactionService {

    CompletableFuture<Transaction> createTransaction(Transaction transaction);
    CompletableFuture<TransactionType> getTransactionType(String type);
}
