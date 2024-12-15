package com.ntros.processor.order.initialization;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InsufficientFundsException;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderType;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.wallet.Wallet;
import com.ntros.processor.order.initialization.create.AbstractCreateOrderInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service("buy")
@Slf4j
public class BuyOrderInitializer extends AbstractCreateOrderInitializer {

    @Override
    protected void validateOrderRequest(CreateOrderRequest request) {
        Wallet wallet = walletService.getWalletByCurrencyCodeAccountNumber(request.getCurrencyCode(), request.getAccountNumber());
        BigDecimal requiredAmount = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        if (wallet.getBalance().compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException(String.format("Not enough funds to complete the buy order. Current funds: %s", wallet.getBalance()));
        }
    }
}
