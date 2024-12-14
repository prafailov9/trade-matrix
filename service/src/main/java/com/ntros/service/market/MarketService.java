package com.ntros.service.market;

import com.ntros.model.market.Market;

import java.util.concurrent.CompletableFuture;

public interface MarketService {


    CompletableFuture<Market> getMarketByCode(String marketCode);


}
