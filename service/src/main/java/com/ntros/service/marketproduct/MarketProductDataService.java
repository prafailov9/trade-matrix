package com.ntros.service.marketproduct;

import com.ntros.exception.NotFoundException;
import com.ntros.marketproduct.MarketProductRepository;
import com.ntros.model.currency.Currency;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class MarketProductDataService implements MarketProductService {

    private final MarketProductRepository marketProductRepository;

    @Autowired
    public MarketProductDataService(MarketProductRepository marketProductRepository) {
        this.marketProductRepository = marketProductRepository;
    }

    @Override
    public CompletableFuture<MarketProduct> getMarketProductByIsinMarketCodeAsync(String isin, String marketCode) {
        return supplyAsync(() -> getMarketProductByIsinMarketCode(isin, marketCode));
    }

    @Override
    public MarketProduct getMarketProductByIsinMarketCode(String isin, String marketCode) {
        return marketProductRepository.findByProductIsinMarketCode(isin, marketCode)
                .orElseThrow(() ->
                        NotFoundException.with(format("Product not found for isin: %s, market_code: %s",
                                isin, marketCode)));
    }

    @Override
    public CompletableFuture<BigDecimal> getMarketPriceForProductAndCurrency(Product product, Currency currency) {
        return supplyAsync(() ->
                marketProductRepository.findMarketPriceForProductCurrency(product, currency)
                        .orElseThrow(() ->
                                NotFoundException.with(format("Failed to find market price for product %s",
                                        product.getProductName()))));
    }

}
