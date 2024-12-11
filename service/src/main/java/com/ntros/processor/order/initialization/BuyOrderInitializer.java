package com.ntros.processor.order.initialization;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InsufficientFundsException;
import com.ntros.model.order.Order;
import com.ntros.processor.order.initialization.create.AbstractCreateOrderInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service("buy")
@Slf4j
public class BuyOrderInitializer extends AbstractCreateOrderInitializer {


    @Override
    public CompletableFuture<Order> initialize(CreateOrderRequest createOrderRequest) {
        Order.OrderBuilder orderBuilder = Order.builder();
        log.info("Initializing order: {}", createOrderRequest);
        // get wallet from DB, validate available balance.
        return walletService.getWalletByCurrencyCodeAndAccountNumber(createOrderRequest.getCurrencyCode(), createOrderRequest.getAccountNumber())
                .thenComposeAsync(wallet -> {
                    orderBuilder.wallet(wallet);
                    validateAvailableFunds(wallet.getBalance(), createOrderRequest.getPrice(), createOrderRequest.getQuantity());
                    return productService.getProduct(createOrderRequest.getProductIsin());
                }, executor)
                // get Product and Order Type
                .thenComposeAsync(product -> {
                    orderBuilder.product(product);
                    return orderService.getOrderType(createOrderRequest.getOrderType());
                }, executor)
                .thenComposeAsync(orderType -> {
                    orderBuilder.orderType(orderType);
                    // Convert DTO to Order model
                    return CompletableFuture.supplyAsync(() -> {
                        orderBuilder.filledQuantity(0); // new order
                        return orderConverter.toModel(createOrderRequest, orderBuilder);
                    }, executor);
                }, executor)
                // save order and set open status
                .thenComposeAsync(this::placeOpenOrderAndSetOpenStatus, executor);
    }

    private void validateAvailableFunds(BigDecimal currentBalance, BigDecimal orderPrice, int quantity) {
        BigDecimal requiredAmount = orderPrice.multiply(BigDecimal.valueOf(quantity));
        if (currentBalance.compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Not enough funds to complete the buy order. Current funds: %s", currentBalance));
        }
    }

}
