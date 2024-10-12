package com.ntros.processor.order.initialization;

import com.ntros.dataservice.position.PositionService;
import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InsufficientAssetsException;
import com.ntros.model.order.Order;
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
    public CompletableFuture<Order> initialize(CreateOrderRequest createOrderRequest) {
        Order.OrderBuilder orderBuilder = Order.builder();
        log.info("Initializing order: {}", createOrderRequest);
        // Step 1: Validate and retrieve wallet, check funds for buy orders
        return walletService.getWalletByCurrencyCodeAndAccountNumber(createOrderRequest.getCurrencyCode(), createOrderRequest.getAccountNumber())
                .thenComposeAsync(wallet -> {
                    orderBuilder.wallet(wallet);
                    return productService.getProduct(createOrderRequest.getProductIsin());
                }, executor)
                .thenComposeAsync(product -> {
                    orderBuilder.product(product);
                    return positionService.getQuantityByAccountNumberAndProductIsin(createOrderRequest.getAccountNumber(), product.getIsin())
                            .thenApplyAsync(positionQuantity -> {
                                if (positionQuantity < createOrderRequest.getQuantity()) {
                                    throw new InsufficientAssetsException(
                                            String.format("Not enough assets to sell. Requested sells=%s, position quantity=%s",
                                                    createOrderRequest.getQuantity(), positionQuantity));
                                }
                                return CompletableFuture.completedFuture(positionQuantity);
                            }, executor);
                }, executor)
                // Step 2: Retrieve Product and Order Type
                .thenComposeAsync(positionQuantity -> orderService.getOrderType(createOrderRequest.getOrderType()), executor)
                .thenComposeAsync(orderType -> {
                    orderBuilder.orderType(orderType);
                    // Convert DTO to Order model
                    return CompletableFuture.supplyAsync(() -> orderConverter.toModel(createOrderRequest, orderBuilder));
                }, executor)

                // Step 3: save order and set status
                .thenComposeAsync(this::placeOpenOrderAndSetOpenStatus, executor);
    }

}
