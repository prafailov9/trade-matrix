package com.ntros.service.currency;

import com.ntros.account.WalletRepository;
import com.ntros.currency.CurrencyRepository;
import com.ntros.exception.DataAccessViolationException;
import com.ntros.exception.NotFoundException;
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

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
        return supplyAsync(() -> currencyRepository.findByCurrencyCode(code)
                .orElseThrow(() -> NotFoundException.with(format("Could not find currency with code:%s", code))));
    }

    @Override
    public Currency getCurrencyByCode(String code) {
        return currencyRepository.findByCurrencyCode(code)
                .orElseThrow(() -> NotFoundException.with(format("Could not find currency with code:%s", code)));
    }

    @Override
    public CompletableFuture<Void> activateAll() {
        return CompletableFuture.runAsync(() -> {
            try {
                currencyRepository.activateAll();
                log.info("All currencies activated successfully");
            } catch (DataAccessException ex) {
                log.error("Error occurred while activating currencies: {}", ex.getMessage(), ex);
                throw DataAccessViolationException.with(ex.getMessage(), ex);
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
                String err = format("Currency with id %s could not be deleted.", currencyId);
                log.error(err, ex);
                throw DataAccessViolationException.with(err, ex);
            }
        });
    }
}