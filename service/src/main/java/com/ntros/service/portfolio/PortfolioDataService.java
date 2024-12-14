package com.ntros.service.portfolio;

import com.ntros.exception.PositionNotFoundException;
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
    public CompletableFuture<Portfolio> getPortfolioByAccountProductIsin(Account account, Product product) {
        return supplyAsync(() ->
                portfolioRepository.findByAccountNumberProductIsin(account.getAccountNumber(), product.getIsin())
                        .orElseThrow(() -> new PositionNotFoundException(
                                String.format("portfolio not found for account: %s, product=%s",
                                        account.getAccountNumber(), product.getProductName()))));
    }
}
