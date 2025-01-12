package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InvalidArgumentException;
import com.ntros.model.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("buy")
@Slf4j
public class BuyOrderInitializer extends AbstractCreateOrderInitializer {

    @Override
    protected void validateOrderRequest(CreateOrderRequest request) {
        Wallet wallet = walletService.getWalletByCurrencyCodeAccountNumber(request.getCurrencyCode(), request.getAccountNumber());
        BigDecimal requiredAmount = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        if (wallet.getBalance().compareTo(requiredAmount) < 0) {
            throw InvalidArgumentException.with(String.format("Not enough funds to complete the buy order. Current funds: %s", wallet.getBalance()));
        }
    }
}
