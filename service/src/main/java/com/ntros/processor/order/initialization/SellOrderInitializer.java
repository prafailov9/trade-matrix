package com.ntros.processor.order.initialization;

import com.ntros.service.position.PositionService;
import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InsufficientAssetsException;
import com.ntros.model.order.Order;
import com.ntros.processor.order.initialization.create.AbstractCreateOrderInitializer;
import com.ntros.processor.order.initialization.create.CreateOrderInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service("sell")
@Slf4j
public class SellOrderInitializer extends AbstractCreateOrderInitializer implements CreateOrderInitializer {


    private final PositionService positionService;

    @Autowired
    public SellOrderInitializer(PositionService positionService) {
        this.positionService = positionService;
    }

    @Override
    public CompletableFuture<Order> initialize(CreateOrderRequest request) {
        Order.OrderBuilder orderBuilder = Order.builder();
        log.info("Initializing order: {}", request);
        // Step 1: Validate and retrieve wallet, check funds for buy orders
        return walletService.getWalletByCurrencyCodeAccountNumber(request.getCurrencyCode(), request.getAccountNumber())
                .thenComposeAsync(wallet -> {
                    orderBuilder.wallet(wallet);

                    return marketProductService.getMarketProductByIsinMarketCode(request.getMarketCode(), request.getProductIsin());
                }, executor)
                .thenComposeAsync(marketProduct -> {
                    orderBuilder.marketProduct(marketProduct);
                    return positionService.getQuantityByAccountNumberAndProductIsin(request.getAccountNumber(), request.getProductIsin())
                            .thenApplyAsync(positionQuantity -> {
                                if (positionQuantity < request.getQuantity()) {
                                    throw new InsufficientAssetsException(
                                            String.format("Not enough assets to sell. Requested sells=%s, position quantity=%s",
                                                    request.getQuantity(), positionQuantity));
                                }
                                return CompletableFuture.completedFuture(positionQuantity);
                            }, executor);
                }, executor)
                // Step 2: Retrieve Product and Order Type
                .thenComposeAsync(positionQuantity -> orderService.getOrderType(request.getOrderType()), executor)
                .thenComposeAsync(orderType -> {
                    orderBuilder.orderType(orderType);
                    // Convert DTO to Order model
                    return CompletableFuture.supplyAsync(() -> orderConverter.toModel(request, orderBuilder));
                }, executor)

                // Step 3: save order and set status
                .thenComposeAsync(this::placeOpenOrderAndSetOpenStatus, executor);
    }

}
