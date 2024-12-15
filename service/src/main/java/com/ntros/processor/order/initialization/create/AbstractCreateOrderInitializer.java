package com.ntros.processor.order.initialization.create;


import com.ntros.converter.order.OrderProcessingConverter;
import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.service.marketproduct.MarketProductService;
import com.ntros.service.order.OrderService;
import com.ntros.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public abstract class AbstractCreateOrderInitializer implements CreateOrderInitializer {

    @Autowired
    @Qualifier("taskExecutor")
    protected Executor executor;
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected WalletService walletService;
    @Autowired
    protected OrderProcessingConverter orderProcessingConverter;

    @Autowired
    protected MarketProductService marketProductService;


    @Transactional
    protected Order placeOpenOrderAndSetOpenStatus(Order openOrder) {
        Order createdOrder = orderService.createOrder(openOrder);
        OrderStatus orderStatus = orderService.updateOrderStatus(createdOrder, CurrentOrderStatus.OPEN);
        createdOrder.setOrderStatuses(List.of(orderStatus));
        log.info("Order initialized: {} for [product = {}, currency = {}, account = {}] with status: {}",
                createdOrder,
                createdOrder.getMarketProduct(),
                createdOrder.getWallet().getCurrency(),
                createdOrder.getWallet().getAccount().getAccountNumber(),
                orderStatus.getCurrentStatus());
        return createdOrder;
    }

}
