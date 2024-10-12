package com.ntros.dataservice.currency;

import com.ntros.account.WalletRepository;
import com.ntros.currency.CurrencyRepository;
import com.ntros.exception.CurrencyNotFoundException;
import com.ntros.exception.FailedToActivateAllCurrenciesException;
import com.ntros.model.currency.Currency;
import com.ntros.model.wallet.Wallet;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Transactional
@Slf4j
public class CurrencyDataService implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final WalletRepository walletRepository;

    @Autowired
    public CurrencyDataService(CurrencyRepository currencyRepository, WalletRepository walletRepository) {
        this.currencyRepository = currencyRepository;
        this.walletRepository = walletRepository;
    }


    // convert an amount from one currency to another

    @Override
    public CompletableFuture<Currency> getCurrencyByCodeAsync(String code) {
        return CompletableFuture.supplyAsync(() -> currencyRepository.findByCurrencyCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException(String.format("Could not find currency with code:%s", code))));
    }

    @Override
    public Currency getCurrencyByCode(String code) {
        return currencyRepository.findByCurrencyCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException(String.format("Could not find currency with code:%s", code)));
    }

    @Override
    public CompletableFuture<Void> activateAll() {
        return CompletableFuture.runAsync(() -> {
            try {
                currencyRepository.activateAll();
                log.info("All currencies activated successfully");
            } catch (DataAccessException ex) {
                log.error("Error occurred while activating currencies: {}", ex.getMessage(), ex);
                throw new FailedToActivateAllCurrenciesException(ex.getMessage(), ex);
            }
        });
    }

    @Override
    @Transactional
    @Modifying
    public CompletableFuture<Void> deleteCurrency(int currencyId) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<Wallet> wallets = walletRepository.findAllByCurrencyId(currencyId);
                walletRepository.deleteAll(wallets);
                currencyRepository.deleteById(currencyId);
            } catch (DataAccessException ex) {
                log.error("Currency with id {} could not be deleted", currencyId, ex);
                throw new CompletionException(ex.getMessage(), ex.getCause());
            }
        });
    }
}