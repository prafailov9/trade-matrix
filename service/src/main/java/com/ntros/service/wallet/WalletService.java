package com.ntros.service.wallet;

import com.ntros.dto.UniqueWalletDTO;
import com.ntros.dto.WalletDTO;
import com.ntros.model.wallet.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WalletService {

    CompletableFuture<Wallet> getWallet(final int walletId);

    CompletableFuture<Wallet> getWalletByCurrencyCodeAccountNumberAsync(final String currencyCode, final String accountNumber);
    Wallet getWalletByCurrencyCodeAccountNumber(final String currencyCode, final String accountNumber);

    CompletableFuture<Wallet> getWalletByCurrencyNameAndAccountId(final String currencyName, final int accountId);
    CompletableFuture<Wallet> getWalletByCurrencyCodeAndAccountId(final String currencyCode, final int accountId);

    /**
     * Blocking method, used in transfer service
     */
    // Wallet doGetWalletByCurrencyNameAndAccountId(final String currencyName, final int accountId);
    CompletableFuture<List<Wallet>> getAllWallets();
    CompletableFuture<List<Wallet>> getAllWalletsByAccount(final int accountId);
    CompletableFuture<Wallet> createWallet(final WalletDTO walletDTO);
    CompletableFuture<Wallet> createWallet(final Wallet wallet);

    CompletableFuture<Integer> deleteWallet(final UniqueWalletDTO uniqueWalletDTO);

    CompletableFuture<Void> updateBalanceAsync(final int walletId, final BigDecimal balance);
    void updateBalance(int walletId, BigDecimal balance);
    CompletableFuture<Wallet> validateBalance(Wallet wallet, BigDecimal price, int quantity);

}
