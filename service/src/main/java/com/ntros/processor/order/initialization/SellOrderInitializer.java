package com.ntros.processor.order.initialization;

import com.ntros.model.Position;
import com.ntros.model.order.OrderType;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.wallet.Wallet;
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

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
        return supplyAsync(() -> {
            Order.OrderBuilder orderBuilder = Order.builder();
            log.info("Initializing order: {}", request);

            int positionQuantity = positionService.getQuantityByAccountNumberAndProductIsin(request.getAccountNumber(), request.getProductIsin());
            if (positionQuantity < request.getQuantity()) {
                throw new InsufficientAssetsException(
                        String.format("Not enough assets to sell. Requested sells=%s, position quantity=%s",
                                request.getQuantity(), positionQuantity));
            }
            Wallet wallet = walletService.getWalletByCurrencyCodeAccountNumber(request.getCurrencyCode(), request.getAccountNumber());
            MarketProduct marketProduct = marketProductService.getMarketProductByIsinMarketCode(request.getMarketCode(), request.getProductIsin());
            OrderType orderType = orderService.getOrderType(request.getOrderType());

            orderBuilder.wallet(wallet);
            orderBuilder.marketProduct(marketProduct);
            orderBuilder.orderType(orderType);

            Order order = orderProcessingConverter.toModel(request, orderBuilder);

            return placeOpenOrderAndSetOpenStatus(order);
        }, executor);
    }

}
