package com.ntros.processor.order.execution;

import com.ntros.service.order.OrderService;
import com.ntros.service.position.PositionService;
import com.ntros.service.wallet.WalletService;
import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import com.ntros.service.transaction.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Will execute a Buy or Sell order at a set limit price or better.
 * BUY order is executed when - limit price >= market price;
 * SELL order is executed when - limit price <= market price;
 */
@Service("limit")
@Slf4j
public class LimitOrderExecutor extends AbstractOrderExecutor implements OrderExecutor {


    public LimitOrderExecutor(Executor executor, OrderService orderService, TransactionService transactionService,
                              PositionService positionService, WalletService walletService) {
        super(executor, orderService, transactionService, positionService, walletService);
    }

    @Override
    protected CompletableFuture<Order> fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
        return CompletableFuture.supplyAsync(() -> {
            int remainingQuantity = incomingOrder.getQuantity();

            // Step 3: Iterate over matching orders
            for (Order matchingOrder : matchingOrders) {
                if (remainingQuantity > 0 && isLimitConditionMet(incomingOrder, matchingOrder)) {
                    int matchedQuantity = Math.min(remainingQuantity, matchingOrder.getQuantity());

                    // Step 4: Execute transaction and reduce quantity
                    remainingQuantity -= matchedQuantity;
                    // Update matched order's remaining quantity
                    matchingOrder.setQuantity(matchingOrder.getQuantity() - matchedQuantity);
                    // TODO: Add logic to adjust wallet balances, update matchingOrder quantity, etc.
                }
            }
            incomingOrder.setQuantity(remainingQuantity);  // Update the remaining unfilled quantity
            return incomingOrder;
        }, executor);
    }

    @Override
    protected CompletableFuture<Void> executeOrderTransaction(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        return null;
    }


    /**
     *  For Buy Orders, market price should be <= limit price
     *  For Sell Orders, market price should be >= limit price
     */
    private boolean isLimitConditionMet(Order incomingOrder, Order matchingOrder) {
        return incomingOrder.getSide().equals(Side.BUY)
                ? matchingOrder.getPrice().compareTo(incomingOrder.getPrice()) <= 0
                : matchingOrder.getPrice().compareTo(incomingOrder.getPrice()) >= 0;

    }
}
