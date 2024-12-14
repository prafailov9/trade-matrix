package com.ntros.service.account;

import com.ntros.account.AccountRepository;
import com.ntros.account.WalletRepository;
import com.ntros.service.currency.CurrencyExchangeRateDataService;
import com.ntros.exception.AccountConstraintFailureException;
import com.ntros.exception.AccountNotFoundException;
import com.ntros.exception.WalletNotFoundException;
import com.ntros.model.account.Account;
import com.ntros.model.wallet.Wallet;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.ntros.service.currency.CurrencyUtils.getScale;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Transactional
@Slf4j
public class AccountDataService implements AccountService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final CurrencyExchangeRateDataService currencyExchangeRateDataService;

    @Autowired
    AccountDataService(final AccountRepository accountRepository,
                       final WalletRepository walletRepository,
                       final CurrencyExchangeRateDataService currencyExchangeRateDataService) {
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.currencyExchangeRateDataService = currencyExchangeRateDataService;
    }

    @Override
    public CompletableFuture<Account> getAccount(int accountId) {
        return supplyAsync(() -> accountRepository.findById(accountId)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found for id: " + accountId)));
    }

    @Override
    public CompletableFuture<Account> getAccountByAccountNumber(String accountNumber) {
        return supplyAsync(() -> accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found for AN: " + accountNumber)));
    }

    @Override
    public CompletableFuture<List<Account>> getAllAccounts() {
        return supplyAsync(accountRepository::findAll);
    }

    @Override
    public CompletableFuture<List<Account>> getAllAccountsWalletCount(int walletCount) {
        return supplyAsync(() -> accountRepository.findAllByWalletCount(walletCount));

    }

    @Override
    public CompletableFuture<List<List<Account>>> getAllAccountsByWalletCountInRange(int origin, int bound) {
        return supplyAsync(() -> {
                    int realBound = bound;
                    List<Wallet> wallets = walletRepository.findAll();
                    if (!CollectionUtils.isEmpty(wallets)) {
                        realBound = wallets.size();
                    }
                    List<List<Account>> accountsByWalletCount = new ArrayList<>();
                    int currentOrigin = origin;
                    while (currentOrigin <= realBound) {
                        List<Account> accounts = accountRepository.findAllByWalletCount(currentOrigin);
                        if (!CollectionUtils.isEmpty(accounts)) {
                            accountsByWalletCount.add(accounts);
                        }
                        currentOrigin++;
                    }
                    return accountsByWalletCount;
                });
    }

    @Override
    public CompletableFuture<Account> createAccount(Account account) {
        return supplyAsync(() -> {
                    try {
                        return accountRepository.save(account);
                    } catch (DataIntegrityViolationException ex) {
                        log.error("Could not save account {}. {}", account, ex.getMessage(), ex);
                        throw new AccountConstraintFailureException(account);
                    }
                });
    }

    @Override
    @Modifying
    @Transactional
    public String calculateTotalBalanceForAllAccounts() {
        List<Account> accounts = accountRepository.findAll()
                .stream()
                .filter(account -> !CollectionUtils.isEmpty(account.getWallets()))
                .toList();
        StringBuilder res = new StringBuilder();
        for (Account account : accounts) {
            List<Wallet> wallets = account.getWallets();
            if (wallets.size() == 1) {
                BigDecimal balance = wallets.get(0)
                        .getBalance()
                        .setScale(getScale(wallets.get(0).getBalance()), RoundingMode.HALF_UP);

                account.setTotalBalance(balance);
                res.append(String.format("Total balance for Account [ID: %s]=%s %s for 1 wallet\n",
                        account.getAccountId(), balance, wallets.get(0).getCurrency().getCurrencyCode()));
            } else {
                // get main currency wallet or set 1st to main
                Wallet main = getOrSetMainWallet(wallets);
                main.setMain(true);
                wallets = wallets.stream().filter(wallet -> !wallet.isMain()).collect(Collectors.toList());
                BigDecimal amount = getTotal(wallets, account.getAccountId(), main);
                BigDecimal scaledAmount = amount.setScale(getScale(amount), RoundingMode.HALF_UP);
                account.setTotalBalance(scaledAmount);
                log.info("Saving total balance {} {}", account.getTotalBalance(), main.getCurrency().getCurrencyCode());
                // update balance
                accountRepository.saveAndFlush(account);
                res.append(String.format("Total balance for Account [ID: %s]=%s %s for %s wallets\n",
                        account.getAccountId(), account.getTotalBalance(), main.getCurrency().getCurrencyCode(), account.getWallets().size()));
            }
        }
        return res.toString();
    }

    @Override
    public Account updateTotalBalance(Account account) {
        List<Wallet> wallets = walletRepository.findAllByAccount(account.getAccountId());
        if (CollectionUtils.isEmpty(wallets)) {
            log.info("No wallets tied to account: {}", account.getAccountId());
            return null;
        }
        Wallet mainWallet = getOrSetMainWallet(wallets);
        BigDecimal totalAccountBalance = getTotal(wallets, account.getAccountId(), mainWallet);

        account.setTotalBalance(totalAccountBalance);
        log.info("Updated total balance for account: {}", account.getTotalBalance());
        accountRepository.saveAndFlush(account);
        return account;
    }

    @Override
    @Modifying
    @Transactional
    public CompletableFuture<Account> calculateTotalBalanceForAccount(final Account account) {
        return supplyAsync(() -> updateTotalBalance(account))
                .thenComposeAsync(this::createAccount);
    }

    private BigDecimal getTotal(List<Wallet> wallets, int accountId, Wallet main) {
        if (CollectionUtils.isEmpty(wallets)) {
            log.info("No wallets for accountId: {}", accountId);
            throw new WalletNotFoundException(String.format("No wallets tied to accountId: %s", accountId));
        }
        // convert each to main currency and add
        return wallets
                .stream()
                .map(wallet -> currencyExchangeRateDataService.convert(
                        wallet.getBalance(),
                        wallet.getCurrency(),
                        main.getCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Wallet getOrSetMainWallet(List<Wallet> wallets) {
        Wallet main = null;
        for (Wallet wallet : wallets) {
            if (wallet.isMain()) {
                if (main == null) {
                    main = wallet;
                } else {
                    wallet.setMain(false);
                }
            }
        }
        if (main == null) {
            main = wallets.get(0);
            main.setMain(true);
        }
        return main;
    }
}
