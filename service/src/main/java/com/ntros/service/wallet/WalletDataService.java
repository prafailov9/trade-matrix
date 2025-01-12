package com.ntros.service.wallet;

import com.ntros.account.AccountRepository;
import com.ntros.account.WalletRepository;
import com.ntros.converter.WalletConverter;
import com.ntros.currency.CurrencyRepository;
import com.ntros.dto.UniqueWalletDTO;
import com.ntros.dto.WalletDTO;
import com.ntros.exception.*;
import com.ntros.model.account.Account;
import com.ntros.model.currency.Currency;
import com.ntros.model.wallet.Wallet;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
@Transactional
public class WalletDataService implements WalletService {

    private final Executor executor;
    private final WalletRepository walletRepository;
    private final WalletConverter walletConverter;
    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;


    @Autowired
    public WalletDataService(@Qualifier("taskExecutor") Executor executor, final WalletRepository walletRepository,
                             final WalletConverter walletConverter,
                             final AccountRepository accountRepository,
                             final CurrencyRepository currencyRepository) {
        this.executor = executor;
        this.walletRepository = walletRepository;
        this.walletConverter = walletConverter;
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public CompletableFuture<Wallet> getWallet(int walletId) {
        return supplyAsync(
                () -> walletRepository.findById(walletId)
                        .orElseThrow(() -> {
                            String err = format("Wallet not found for id: %s", walletId);
                            log.error(err);
                            return NotFoundException.with(err);
                        }), executor);
    }

    @Override
    public CompletableFuture<Wallet> getWalletByCurrencyCodeAccountNumberAsync(String currencyCode, String accountNumber) {
        return supplyAsync(() -> getWalletByCurrencyCodeAccountNumber(currencyCode, accountNumber), executor);
    }

    @Override
    public Wallet getWalletByCurrencyCodeAccountNumber(String currencyCode, String accountNumber) {
        return walletRepository
                .findByCurrencyCodeAccountNumber(currencyCode, accountNumber)
                .orElseThrow(() -> {
                    String err = format("Wallet not found for [%s, %s]", currencyCode, accountNumber);
                    log.error(err);
                    return NotFoundException.with(err);
                });
    }

    @Override
    public CompletableFuture<Wallet> getWalletByCurrencyNameAndAccountId(String currencyName, int accountId) {
        return supplyAsync(() -> walletRepository.findByCurrencyNameAccountId(currencyName, accountId)
                .orElseThrow(() -> {
                    String err = format("Wallet not found for [%s, %s]", currencyName, accountId);
                    log.error(err);
                    return NotFoundException.with(err);
                }), executor);
    }

    @Override
    public CompletableFuture<Wallet> getWalletByCurrencyCodeAndAccountId(String currencyCode, int accountId) {
        return supplyAsync(() -> walletRepository.findByCurrencyCodeAccountId(currencyCode, accountId)
                .orElseThrow(() -> {
                    String err = format("Wallet not found for [%s, %s]", currencyCode, accountId);
                    log.error(err);
                    return NotFoundException.with(err);
                }), executor);
    }

    @Override
    public CompletableFuture<List<Wallet>> getAllWallets() {
        return supplyAsync(walletRepository::findAll, executor);
    }

    @Override
    public CompletableFuture<List<Wallet>> getAllWalletsByAccount(final int accountId) {
        return supplyAsync(() -> walletRepository.findAllByAccount(accountId), executor)
                .exceptionally(ex -> {
                    String err = format("Wallets for Account with ID: %s not found.", accountId);
                    log.error(err, ex.getCause());
                    throw NotFoundException.with(err);
                });
    }

    @Override
    public CompletableFuture<Wallet> createWallet(WalletDTO walletDTO) {
        return supplyAsync(() -> walletConverter.toModel(walletDTO), executor)
                .thenApply(wallet ->
                        getAndSetCurrencyAccount(walletDTO.getCurrencyCode(), walletDTO.getAccountNumber(), wallet))
                .thenApply(this::updateActiveWallet)
                .thenApply(this::create);
    }

    @Override
    public CompletableFuture<Wallet> createWallet(Wallet wallet) {
        return supplyAsync(() -> create(wallet));
    }

    @Override
    public CompletableFuture<Integer> deleteWallet(UniqueWalletDTO uniqueWalletDTO) {
        return supplyAsync(() ->
                        delete(
                                uniqueWalletDTO.getCurrencyCode(),
                                uniqueWalletDTO.getAccountNumber()),
                executor);
    }

    @Override
    public void updateBalance(Wallet wallet, BigDecimal balance) {
        try {
            walletRepository.updateBalance(wallet, balance);
        } catch (DataAccessException ex) {
            String err = format("Could not update balance for wallet: %s", wallet);
            log.error(err, ex);
            // rethrowing as a completion exception so it is caught by exceptionally block in thenCombine
            throw new CompletionException(err, DataAccessViolationException.with(err));
        }
    }

    @Override
    public CompletableFuture<Wallet> validateBalance(Wallet wallet, BigDecimal price, int quantity) {
        return supplyAsync(() -> {
            BigDecimal totalOrderValue = price.multiply(BigDecimal.valueOf(quantity));
            if (wallet.getBalance().compareTo(totalOrderValue) < 0) {
                String err = format("Not enough balance for order. Current balance: [%s], Order price: [%s]",
                        wallet.getBalance(), totalOrderValue);
                log.error(err);
                throw InvalidArgumentException.with(err);
            }
            return wallet;
        }, executor);
    }


    private int delete(String code, String an) {
        int affectedRows;
        try {
            affectedRows = walletRepository.deleteByCurrencyCodeAccountNumber(code, an);
            if (affectedRows > 1) {
                log.error("Modified {} rows after delete.", affectedRows);
                throw DataConstraintFailureException.with(format("Deleted multiple wallets for [%s, %s]", code, an));
            }
            log.info("Successfully deleted {} wallet for account: {}", code, an);
        } catch (DataAccessException ex) {
            String err = format("Could not delete wallet for currency code: %s and account number: %s", code, an);
            log.error(err, ex);
            throw DataConstraintFailureException.with(err, ex);
        }
        return affectedRows;
    }

    private Wallet getAndSetCurrencyAccount(String currencyCode, String accountNumber, Wallet wallet) {
        Currency currency = currencyRepository.findByCurrencyCode(currencyCode).orElseThrow(() ->
                NotFoundException.with(format("Currency [%s] doesnt exist.", currencyCode)));
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() ->
                NotFoundException.with(format("Account with AN:[%s] doesnt exist.", accountNumber)));

        wallet.setCurrency(currency);
        wallet.setAccount(account);
        return wallet;
    }

    private Wallet updateActiveWallet(Wallet wallet) {
        if (wallet.isMain()) {
            wallet.getAccount().setWallets(wallet.getAccount().getWallets()
                    .stream()
                    .peek(w -> w.setMain(Boolean.FALSE))
                    .collect(Collectors.toList()));
            accountRepository.saveAndFlush(wallet.getAccount());
        }
        return wallet;
    }

    private Wallet create(Wallet wallet) {
        try {
            return walletRepository.save(wallet);
        } catch (DataIntegrityViolationException ex) {
            log.error("Failed to save wallet: {}", wallet);
            throw DataConstraintFailureException.with(format("Could not create wallet for currency and account [%s, %s]",
                            wallet.getCurrency().getCurrencyCode(),
                            wallet.getAccount().getAccountNumber()),
                    ex.getCause());
        }
    }

}
