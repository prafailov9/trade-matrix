package com.ntros.service.transaction;

import com.ntros.exception.FailedOrdersDeleteException;
import com.ntros.exception.TransactionSaveFailedException;
import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;
import com.ntros.transaction.TransactionRepository;
import com.ntros.transaction.TransactionTypeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    @Transactional
    @Modifying
    @Override
    public CompletableFuture<Transaction> createTransaction(Transaction transaction) {
        return supplyAsync(() -> {
            try {
                Transaction saved = transactionRepository.save(transaction);
                log.info("saved transaction: {}", saved);
                return saved;
            } catch (DataIntegrityViolationException ex) {
                log.error("error while creating transaction {} for [product={}, account={}]", transaction,
                        transaction.getMarketProduct(), transaction.getWallet().getAccount());
                throw new TransactionSaveFailedException(ex.getMessage(), ex);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Transaction>> getAllTransactions() {
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
    public CompletableFuture<TransactionType> getTransactionType(String type) {
        return supplyAsync(() -> transactionTypeRepository.findOneByTransactionTypeName(type)
                .orElseThrow(() -> new FailedOrdersDeleteException(String.format("Could not find tx type with name: %s", type)))
        );
    }


    /**
     * @Override
     *     @Transactional
     *     public void transferFunds(Wallet fromWallet, Wallet toWallet, BigDecimal amount) {
     *         fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
     *         toWallet.setBalance(toWallet.getBalance().add(amount));
     *
     *         walletRepository.save(fromWallet);
     *         walletRepository.save(toWallet);
     *
     *         log.info("Transferred {} from Wallet {} to Wallet {}", amount, fromWallet.getWalletId(), toWallet.getWalletId());
     *     }
     *
     *     @Override
     *     @Transactional
     *     public void transferAssets(Account fromAccount, Product product, int quantity) {
     *         Position fromPosition = positionRepository.findByAccountAndProduct(fromAccount, product)
     *                 .orElseThrow(() -> new IllegalArgumentException("Position not found"));
     *
     *         if (fromPosition.getQuantity() < quantity) {
     *             throw new IllegalStateException("Insufficient assets to transfer");
     *         }
     *
     *         fromPosition.setQuantity(fromPosition.getQuantity() - quantity);
     *
     *         positionRepository.save(fromPosition);
     *
     *         log.info("Transferred {} units of Product {} from Account {}", quantity, product.getProductName(), fromAccount.getAccountId());
     *     }
     */

}