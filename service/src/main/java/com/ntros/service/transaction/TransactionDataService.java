package com.ntros.service.transaction;

import com.ntros.exception.DataConstraintFailureException;
import com.ntros.exception.NotFoundException;
import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;
import com.ntros.transaction.TransactionRepository;
import com.ntros.transaction.TransactionTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class TransactionDataService implements TransactionService {

    private final Executor executor;

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    @Autowired
    public TransactionDataService(Executor executor,
                                  TransactionRepository transactionRepository,
                                  TransactionTypeRepository transactionTypeRepository) {
        this.executor = executor;
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        try {
            return transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException ex) {
            log.error("Error while creating transaction {} for [product={}, account={}]", transaction,
                    transaction.getMarketProduct(), transaction.getWallet().getAccount());
            throw DataConstraintFailureException.with(ex.getMessage(), ex);
        }
    }

    @Override
    public CompletableFuture<List<Transaction>> getAllTransactionsAsync() {
        return supplyAsync(transactionRepository::findAll);
    }

    @Override
    public CompletableFuture<List<Transaction>> getAllTransactionsByPortfolio(String portfolioName) {
        return supplyAsync(() -> transactionRepository.findAllByPortfolioName(portfolioName));
    }

    @Override
    public CompletableFuture<List<Transaction>> getAllTransactionsByAccountNumber(String accountNumber) {
        return supplyAsync(() -> transactionRepository.findAllByAccountNumber(accountNumber));
    }

    @Override
    public TransactionType getTransactionType(String type) {
        return transactionTypeRepository.findOneByTransactionTypeName(type)
                .orElseThrow(() -> NotFoundException.with(format("Could not find tx type with name: %s", type)));
    }
}
