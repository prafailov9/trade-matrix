package com.ntros.service.portfolio;

import com.ntros.model.account.Account;
import com.ntros.model.portfolio.Portfolio;

import java.util.concurrent.CompletableFuture;

public interface PortfolioService {
    CompletableFuture<Portfolio> getPortfolioByAccountNumberAsync(String accountNumber);

    Portfolio getPortfolioByAccountNumber(String accountNumber);
    Portfolio getPortfolioByAccount(Account account);

}
