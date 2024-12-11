package com.ntros.dataservice.market;

import com.ntros.exception.MarketPriceNotFoundException;
import com.ntros.market.MarketRepository;
import com.ntros.model.currency.Currency;
import com.ntros.model.product.Product;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;


@Service
@Slf4j
@Transactional
public class MarketDataService implements MarketService {

    private final MarketRepository marketRepository;


    @Autowired
    public MarketDataService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Override
    public CompletableFuture<BigDecimal> getMarketPriceForProduct(Product product) {
        return null;
    }

    @Override
    public CompletableFuture<BigDecimal> getMarketPriceForProductAndCurrency(Product product, Currency currency) {
        return supplyAsync(() ->
                marketRepository.findMarketPriceForProductCurrency(product, currency)
                        .orElseThrow(() -> new MarketPriceNotFoundException(String.format("Failed to find market price for product %s", product.getProductName()))));
    }
}
