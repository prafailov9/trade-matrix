package com.ntros.processor.order.execution;

import com.ntros.model.order.Order;
import com.ntros.model.order.MatchedOrdersHolder;
import com.ntros.service.order.OrderService;
import com.ntros.service.portfolio.PortfolioService;
import com.ntros.service.position.PositionService;
import com.ntros.service.transaction.TransactionService;
import com.ntros.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * A stop order becomes a market order when the market price reaches or exceeds the stop price.
 * This means it should only execute once the stop price is triggered, converting it into a market order.
 */
@Service("stop")
@Slf4j
public class StopOrderExecutor extends AbstractOrderExecutor implements OrderExecutor {


    public StopOrderExecutor(Executor executor, OrderService orderService, TransactionService transactionService, PositionService positionService, WalletService walletService, PortfolioService portfolioService) {
        super(executor, orderService, transactionService, positionService, walletService, portfolioService);
    }


    @Override
    public MatchedOrdersHolder fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
        return null;
    }


//
//    @Override
//    public CompletableFuture<Order> execute(Order order) {
//        // Step 1: Check if stop condition is triggered
//        if (isStopConditionMet(order)) {
//            log.info("Stop price triggered for order: {}", order.getOrderId());
//            // Step 2: Execute as a market order
//            return executeAsMarketOrder(order);
//        } else {
//            log.info("Stop condition not met for order: {}", order.getOrderId());
//            return CompletableFuture.completedFuture(order);  // No execution, keep order open
//        }
//    }
//
//    private boolean isStopConditionMet(Order order) {
//        // For Buy Stop Order, the market price must be >= stop price
//        // For Sell Stop Order, the market price must be <= stop price
//        return marketService.getMarketPriceForProductAndCurrency(order.getProduct(), order.getWallet().getCurrency())
//                .thenComposeAsync(marketPrice -> {
//                    if (order.getSide().equals(Side.BUY)) {
//                        return marketPrice.compareTo(order.getPrice()) >= 0;
//                    } else {
//                        return marketPrice.compareTo(order.getPrice()) <= 0;
//                    }
//
//                }, executor);
//    }
//
//    private CompletableFuture<Order> executeAsMarketOrder(Order order) {
//        // Step 3: Execute the order like a market order
//        return orderService.findMatchingOrders(order)
//                .thenComposeAsync(matchingOrders -> {
//                    if (matchingOrders.isEmpty()) {
//                        log.info("No matching orders found for stop order: {}", order.getOrderId());
//                        return CompletableFuture.completedFuture(order);  // No matches, keep order open
//                    }
//                    return executeFulfillment(order, matchingOrders);
//                }, executor);
//    }
//
//    private CompletableFuture<Order> executeFulfillment(Order incomingOrder, List<Order> matchingOrders) {
//        return CompletableFuture.supplyAsync(() -> {
//                    int fulfilledQuantity = incomingOrder.getQuantity();
//
//                    for (Order matchingOrder : matchingOrders) {
//                        if (fulfilledQuantity > 0) {
//                            int matchedQuantity = Math.min(fulfilledQuantity, matchingOrder.getQuantity());
//                            fulfilledQuantity -= matchedQuantity;
//                            // TODO: Add transaction logic here
//                        }
//                    }
//
//                    incomingOrder.setQuantity(fulfilledQuantity);  // Update remaining quantity
//                    return incomingOrder;
//                }, executor)
//                .thenComposeAsync(order -> orderService.determineAndUpdateCurrentStatus(order).thenApplyAsync(orderStatus -> {
//                    order.getOrderStatusList().add(orderStatus);
//                    return orderService.createOrder(order).thenApplyAsync(savedOrder -> order, executor);
//                }), executor);
//    }
//
//    @Override
//    protected CompletableFuture<Order> fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
//        return null;
//    }
}
