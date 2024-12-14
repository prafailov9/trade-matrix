package com.ntros.service.marketproduct;

import com.ntros.model.currency.Currency;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.product.Product;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface MarketProductService {

    CompletableFuture<MarketProduct> getMarketProductByIsinMarketCode(String isin, String marketCode);
    CompletableFuture<BigDecimal> getMarketPriceForProductAndCurrency(Product product, Currency currency);

}
