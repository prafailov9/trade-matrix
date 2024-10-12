package com.ntros.dataservice.market;

import com.ntros.model.currency.Currency;
import com.ntros.model.product.Product;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface MarketService {

    CompletableFuture<BigDecimal> getMarketPriceForProduct(Product product);
    CompletableFuture<BigDecimal> getMarketPriceForProductAndCurrency(Product product, Currency currency);


}
