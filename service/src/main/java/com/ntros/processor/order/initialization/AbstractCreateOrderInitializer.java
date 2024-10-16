package com.ntros.processor.order.initialization;


import com.ntros.converter.order.OrderConverter;
import com.ntros.dataservice.order.OrderService;
import com.ntros.dataservice.product.ProductService;
import com.ntros.dataservice.wallet.WalletService;
import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
    protected OrderConverter orderConverter;

    @Autowired
    protected ProductService productService;


    protected CompletableFuture<Order> placeOpenOrderAndSetOpenStatus(Order openOrder) {
        return orderService.createOrder(openOrder)
                .thenComposeAsync(order -> orderService.updateOrderStatus(order, CurrentOrderStatus.OPEN))
                .thenApplyAsync(orderStatus -> {
                    openOrder.setOrderStatusList(List.of(orderStatus));
                    log.info("Order initialized: {} for [product = {}, currency = {}, account = {}] with status: {}",
                            openOrder,
                            openOrder.getProduct().getProductName(),
                            openOrder.getWallet().getCurrency(),
                            openOrder.getWallet().getAccount().getAccountNumber(),
                            orderStatus.getCurrentStatus());
                    return openOrder;
                }, executor);
    }

}
