package com.ntros.service.portfolio;

import com.ntros.exception.NotFoundException;
import com.ntros.model.account.Account;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.product.Product;
import com.ntros.portfolio.PortfolioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class PortfolioDataService implements PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Autowired
    public PortfolioDataService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    public CompletableFuture<Portfolio> getPortfolioByAccountNumberAsync(String accountNumber) {
        return supplyAsync(() -> getPortfolioByAccountNumber(accountNumber));
    }

    @Override
    public Portfolio getPortfolioByAccountNumber(String accountNumber) {
        return portfolioRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> NotFoundException.with(
                        String.format("Portfolio not found for account: %s",
                                accountNumber)));
    }

    @Override
    public Portfolio getPortfolioByAccount(Account account) {
        return portfolioRepository.findByAccount(account)
                .orElseThrow(() ->
                        NotFoundException.with(String.format("Portfolio not found for account: %s", account)));
    }
}
