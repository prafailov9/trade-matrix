package com.ntros.service.market;

import com.ntros.exception.NotFoundException;
import com.ntros.market.MarketRepository;
import com.ntros.model.market.Market;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
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
    public CompletableFuture<Market> getMarketByCode(String marketCode) {
        return supplyAsync(() -> marketRepository.findByMarketCode(marketCode).orElseThrow(
                () -> NotFoundException.with(format("Market not found for code: %s", marketCode))));
    }
}
