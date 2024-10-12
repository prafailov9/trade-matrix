package com.ntros.processor.order.execution;

import com.ntros.dataservice.order.OrderService;
import com.ntros.dataservice.position.PositionService;
import com.ntros.dataservice.wallet.WalletService;
import com.ntros.exception.BalanceAdjustmentException;
import com.ntros.exception.OrderProcessingException;
import com.ntros.model.order.Order;
import com.ntros.processor.transaction.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public abstract class AbstractOrderExecutor implements OrderExecutor {

    protected final Executor executor;
    protected final OrderService orderService;
    protected final TransactionService transactionService;
    protected final PositionService positionService;
    protected final WalletService walletService;

    @Autowired
    public AbstractOrderExecutor(@Qualifier("taskExecutor") Executor executor, OrderService orderService,
                                 TransactionService transactionService, PositionService positionService,
                                 WalletService walletService) {
        this.executor = executor;
        this.orderService = orderService;
        this.transactionService = transactionService;
        this.positionService = positionService;
        this.walletService = walletService;
    }

    @Override
    public CompletableFuture<Order> execute(Order order) {
        return orderService.findMatchingOrders(order)
                .thenComposeAsync(matchingOrders -> {
                    if (matchingOrders.isEmpty()) {
                        log.info("No matching orders found for limit order: {}", order.getOrderId());
                        return CompletableFuture.completedFuture(order);  // No matches, keep order open
                    }
                    // Step 2: Execute fulfillment for matching orders
                    return executeFulfillment(order, matchingOrders);
                }, executor)
                .exceptionally(ex -> {
                    throw new OrderProcessingException(ex.getMessage(), ex);
                });
    }

    private CompletableFuture<Order> executeFulfillment(Order incomingOrder, List<Order> matchingOrders) {
        return fulfillOrders(incomingOrder, matchingOrders)
                .thenComposeAsync(orderService::determineAndUpdateCurrentStatus, executor)
                .thenComposeAsync(orderStatus -> {
                    incomingOrder.getOrderStatusList().add(orderStatus);
                    return orderService.createOrder(incomingOrder);
                }, executor);
    }


    protected CompletableFuture<Void> adjustWalletBalances(Order buyOrder, Order sellOrder, int matchedQuantity) {
        BigDecimal orderPrice = sellOrder.getPrice();  // Use the sell order's price
        BigDecimal totalCost = orderPrice.multiply(BigDecimal.valueOf(matchedQuantity));

        CompletableFuture<Void> buyerUpdate = walletService.updateBalance(buyOrder.getWallet().getWalletId(), totalCost.negate());
        CompletableFuture<Void> sellerUpdate = positionService.updatePosition(sellOrder.getWallet().getAccount(), sellOrder.getProduct(), matchedQuantity);

        return CompletableFuture.allOf(buyerUpdate, sellerUpdate)
                .thenRunAsync(() ->
                        log.info("Successfully updated balances. Buyer debited: {}, Seller debited shares: {}",
                                totalCost, matchedQuantity)
                        , executor)
                .exceptionally(ex -> {
                    log.error("Failed to adjust wallet or position balances: {}", ex.getMessage());
                    throw new BalanceAdjustmentException("Error adjusting balances", ex);
                });
    }

    protected abstract CompletableFuture<Order> fulfillOrders(Order incomingOrder, List<Order> matchingOrders);

    protected abstract CompletableFuture<Void> executeOrderTransaction(Order incomingOrder, Order matchingOrder, int matchedQuantity);

}
