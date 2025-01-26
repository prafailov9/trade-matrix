package com.ntros.processor.order.execution;

import com.ntros.exception.OrderProcessingException;
import com.ntros.model.Position;
import com.ntros.model.order.MatchedOrdersHolder;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.model.order.Side;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;
import com.ntros.model.wallet.Wallet;
import com.ntros.processor.order.fulfillment.OrderFulfillment;
import com.ntros.service.order.OrderService;
import com.ntros.service.portfolio.PortfolioService;
import com.ntros.service.position.PositionService;
import com.ntros.service.transaction.TransactionService;
import com.ntros.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.ntros.model.order.Side.BUY;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public abstract class AbstractOrderExecutor implements OrderExecutor, OrderFulfillment {

    protected final Executor taskExecutor;
    protected final OrderService orderService;
    protected final TransactionService transactionService;
    protected final PositionService positionService;
    protected final WalletService walletService;
    protected final PortfolioService portfolioService;

    @Autowired
    public AbstractOrderExecutor(@Qualifier("taskExecutor") Executor taskExecutor, OrderService orderService,
                                 TransactionService transactionService, PositionService positionService,
                                 WalletService walletService, PortfolioService portfolioService) {
        this.taskExecutor = taskExecutor;
        this.orderService = orderService;
        this.transactionService = transactionService;
        this.positionService = positionService;
        this.walletService = walletService;
        this.portfolioService = portfolioService;
    }

    @Override
    public CompletableFuture<Order> execute(Order order) {
        return supplyAsync(() -> {
            List<Order> matchingOrders = orderService.findMatchingOrders(order);
            if (matchingOrders.isEmpty()) {
                log.info("No matching orders found for order: {}", order.getOrderId());
                return order;  // no matches, keep order open
            }
            return executeFulfillment(order, matchingOrders);
        }, taskExecutor)
                .exceptionally(ex -> {
                    throw new OrderProcessingException(ex.getMessage(), ex);
                });
    }

    protected void transferFundsAndAssets(Order buyOrder, Order sellOrder, int matchedQuantity) {
        // Use SELL order's price
        BigDecimal orderPrice = sellOrder.getPrice();
        BigDecimal totalCost = orderPrice.multiply(BigDecimal.valueOf(matchedQuantity));
        log.info("totalCost:{}, buyOrder:{}, sellOrder:{}", totalCost, buyOrder, sellOrder);

        Wallet buyOrderWallet = buyOrder.getWallet();
        Wallet sellOrderWallet = sellOrder.getWallet();

        // update wallets for both orders
        buyOrderWallet.deductBalance(totalCost);
        sellOrderWallet.increaseBalance(totalCost);
        walletService.updateBalance(buyOrderWallet, buyOrderWallet.getBalance());
        walletService.updateBalance(sellOrderWallet, sellOrderWallet.getBalance());

        buyOrder.adjustQuantity(matchedQuantity);
        sellOrder.adjustQuantity(matchedQuantity);
        // OPEN buy orders don't have positions
        positionService.updatePosition(sellOrderWallet.getAccount(),
                sellOrder.getMarketProduct().getProduct(), matchedQuantity,
                sellOrder.getSide());
    }

    @Transactional
    private Order executeFulfillment(Order incomingOrder, List<Order> matchingOrders) {
        MatchedOrdersHolder fulfilledOrders = fulfillOrders(incomingOrder, matchingOrders);

        saveFulfilledOrders(fulfilledOrders.getAllOrders());
        createBuyOrderPositions(fulfilledOrders.getAllOrders());
        createOrderTransactions(fulfilledOrders.getAllOrders());
        return fulfilledOrders.getIncomingOrder();
    }

    private void saveFulfilledOrders(List<Order> fulfilledOrders) {
        fulfilledOrders.forEach(order -> {
            // will update statuses and remove FILLED orders from OrderBook
            OrderStatus status = orderService.determineAndUpdateCurrentStatus(order);
            List<OrderStatus> updatedOrderStatuses = new ArrayList<>(order.getOrderStatuses());
            updatedOrderStatuses.add(status);
            order.setOrderStatuses(updatedOrderStatuses);

            orderService.createOrder(order);
        });
    }

    private void createBuyOrderPositions(List<Order> savedOrders) {
        savedOrders.stream()
                .filter(order -> order.getSide().equals(BUY))
                .forEach(buyOrder -> {
                    Portfolio portfolio = portfolioService.getPortfolioByAccountNumber(
                            buyOrder.getWallet().getAccount().getAccountNumber());

                    Position position = new Position();
                    position.setQuantity(buyOrder.getQuantity());
                    position.setProduct(buyOrder.getMarketProduct().getProduct());
                    position.setPortfolio(portfolio);
                    positionService.createPosition(position);
                });
    }

    private void createOrderTransactions(List<Order> savedOrders) {
        List<Transaction> txs = savedOrders.stream()
                .map(order -> {
                    TransactionType transactionType = transactionService.getTransactionType(order.getSide().name());
                    Portfolio portfolio = portfolioService.getPortfolioByAccount(order.getWallet().getAccount());

                    Transaction transaction = Transaction.builder()
                            .order(order)
                            .marketProduct(order.getMarketProduct())
                            .wallet(order.getWallet())
                            .currency(order.getWallet().getCurrency().getCurrencyCode())
                            .price(order.getPrice())
                            .quantity(order.getFilledQuantity())
                            .transactionType(transactionType)
                            .portfolio(portfolio)
                            .transactionDate(OffsetDateTime.now())
                            .build();

                    log.info("Saving transaction: {}", transaction);
                    return transactionService.createTransaction(transaction);
                }).toList();
        log.info("transactions successfully saved: {}", txs);
    }
}
