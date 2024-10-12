package com.ntros.dataservice.portfolio;

import com.ntros.model.account.Account;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.product.Product;

import java.util.concurrent.CompletableFuture;

public interface PortfolioService {

    CompletableFuture<Portfolio> getPortfolioByAccountProductIsin(Account account, Product product);

}
