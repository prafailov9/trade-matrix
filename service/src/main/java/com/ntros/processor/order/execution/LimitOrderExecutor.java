package com.ntros.processor.order.execution;

import com.ntros.model.order.MatchedOrdersHolder;
import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
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
 * Will execute a Buy or Sell order at a set limit price or better.
 * BUY order is executed when - limit price >= market price;
 * SELL order is executed when - limit price <= market price;
 */
@Service("limit")
@Slf4j
public class LimitOrderExecutor extends AbstractOrderExecutor {


    public LimitOrderExecutor(Executor executor, OrderService orderService, TransactionService transactionService,
                              PositionService positionService, WalletService walletService, PortfolioService portfolioService) {
        super(executor, orderService, transactionService, positionService, walletService, portfolioService);
    }

    @Override
    public MatchedOrdersHolder fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
        int incomingOrderRemainingQuantity = incomingOrder.getQuantity();

        for (Order matchingOrder : matchingOrders) {
            log.info("Fulfilling orders. incoming:{}, matching:{}", incomingOrder, matchingOrder);
            if (incomingOrderRemainingQuantity > 0 && isLimitConditionMet(incomingOrder, matchingOrder)) {
                int matchedQuantity = Math.min(incomingOrderRemainingQuantity, matchingOrder.getRemainingQuantity());
                incomingOrderRemainingQuantity -= matchedQuantity;

                updateFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity);
            }
        }

        if (incomingOrderRemainingQuantity > 0) {
            log.info("Incoming Order with ID:{} not fully fulfilled. " +
                            "Waiting for matching orders to satisfy limit price: {}",
                    incomingOrder.getOrderId(),
                    incomingOrder.getPrice());

            //TODO: fulfill remaining quantity in background.
        }

        matchingOrders.add(incomingOrder);
        return MatchedOrdersHolder.of(incomingOrder, matchingOrders);
    }


    protected void updateFundsAndAssets(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        if (incomingOrder.getSide().equals(Side.BUY)) {
            transferFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity);
        } else {
            transferFundsAndAssets(matchingOrder, incomingOrder, matchedQuantity);
        }
    }

    /**
     * For Buy Orders, market price should be <= limit price
     * For Sell Orders, market price should be >= limit price
     */
    private boolean isLimitConditionMet(Order incomingOrder, Order matchingOrder) {
        return incomingOrder.getSide().equals(Side.BUY)
                ? matchingOrder.getPrice().compareTo(incomingOrder.getPrice()) <= 0
                : matchingOrder.getPrice().compareTo(incomingOrder.getPrice()) >= 0;

    }
}
