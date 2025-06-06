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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Immediately executes the order at best available price.
 */
@Service("market")
@Slf4j
public class MarketOrderExecutor extends AbstractOrderExecutor {

    public MarketOrderExecutor(Executor executor, OrderService orderService, TransactionService transactionService,
                               PositionService positionService, WalletService walletService, PortfolioService portfolioService) {
        super(executor, orderService, transactionService, positionService, walletService, portfolioService);
    }

    /**
     * Fulfilling the incoming order's overall asset quantity to the matched order's remaining unfulfilled quantity.
     * Matched orders are sorted by ASC price, meaning the incoming order will be fulfilled with the best possible prices.
     * Order fulfillment continues until the incoming order is fully or partially fulfilled.
     * Partial fulfillment means there are no remaining matching
     * orders to fulfill the incoming order -> matchingOrders.remainingQuantity < incomingOrder.remainingQuantity
     * <p>
     * //     * @param incomingOrder  - order to fulfill
     * //     * @param matchingOrders - matching orders, opposite side to the incoming order to fulfill
     *
     * @return fulfilled incoming order
     */
    @Override
    @Transactional
    public MatchedOrdersHolder fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
        int incomingOrderRemainingQuantity = incomingOrder.getQuantity();

        for (Order matchingOrder : matchingOrders) {
            log.info("Fulfilling orders. incoming:{}, matching:{}", incomingOrder, matchingOrder);
            if (incomingOrderRemainingQuantity > 0) {
                int matchedQuantity = Math.min(incomingOrderRemainingQuantity, matchingOrder.getRemainingQuantity());
                incomingOrderRemainingQuantity -= matchedQuantity;

                updateFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity);
            }
        }

        return MatchedOrdersHolder.of(incomingOrder, matchingOrders);
    }

    protected void updateFundsAndAssets(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        if (incomingOrder.getSide().equals(Side.BUY)) {
            transferFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity);
        } else {
            transferFundsAndAssets(matchingOrder, incomingOrder, matchedQuantity);
        }
    }

}
