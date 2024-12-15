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
    public CompletableFuture<Order> initialize(CreateOrderRequest request) {
        return supplyAsync(() -> {
            Order.OrderBuilder orderBuilder = Order.builder();
            log.info("Initializing order: {}", request);

            Wallet wallet = walletService.getWalletByCurrencyCodeAccountNumber(request.getCurrencyCode(), request.getAccountNumber());
            validateAvailableFunds(wallet.getBalance(), request.getPrice(), request.getQuantity());
            orderBuilder.wallet(wallet);

            MarketProduct marketProduct = marketProductService.getMarketProductByIsinMarketCode(request.getProductIsin(), request.getMarketCode());
            orderBuilder.marketProduct(marketProduct);

            OrderType orderType = orderService.getOrderType(request.getOrderType());
            orderBuilder.orderType(orderType);

            orderBuilder.filledQuantity(0); // new order
            Order order = orderProcessingConverter.toModel(request, orderBuilder);
            return placeOpenOrderAndSetOpenStatus(order);
        }, executor);

    }

    private void validateAvailableFunds(BigDecimal currentBalance, BigDecimal orderPrice, int quantity) {
        BigDecimal requiredAmount = orderPrice.multiply(BigDecimal.valueOf(quantity));
        if (currentBalance.compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException(String.format("Not enough funds to complete the buy order. Current funds: %s", currentBalance));
        }
    }

}
