package com.ntros.processor.order.execution;

import com.ntros.exception.OrderProcessingException;
import com.ntros.model.Position;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.model.order.OrderType;
import com.ntros.model.order.Side;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;
import com.ntros.model.wallet.Wallet;
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

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public abstract class AbstractOrderExecutor implements OrderExecutor {

    protected final Executor executor;
    protected final OrderService orderService;
    protected final TransactionService transactionService;
    protected final PositionService positionService;
    protected final WalletService walletService;
    protected final PortfolioService portfolioService;

    @Autowired
    public AbstractOrderExecutor(@Qualifier("taskExecutor") Executor executor, OrderService orderService,
                                 TransactionService transactionService, PositionService positionService,
                                 WalletService walletService, PortfolioService portfolioService) {
        this.executor = executor;
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
        }, executor).exceptionally(ex -> {
            throw new OrderProcessingException(ex.getMessage(), ex);
        });
    }

    @Transactional
    private Order executeFulfillment(Order incomingOrder, List<Order> matchingOrders) {
        List<Order> fulfilledOrders = fulfillOrders(incomingOrder, matchingOrders);
        List<Order> savedOrders = saveFulfilledOrders(fulfilledOrders);
        createBuyOrderPositions(savedOrders);
        createOrderTransactions(savedOrders);
        return savedOrders.get(savedOrders.size() - 1); // last order is the incoming one
    }

    protected abstract List<Order> fulfillOrders(Order incomingOrder, List<Order> matchingOrders);

    private List<Order> saveFulfilledOrders(List<Order> fulfilledOrders) {
        return fulfilledOrders.stream().map(order -> {
            OrderStatus status = orderService.determineAndUpdateCurrentStatus(order);
            List<OrderStatus> updatedOrderStatuses = new ArrayList<>(order.getOrderStatuses());
            updatedOrderStatuses.add(status);
            order.setOrderStatuses(updatedOrderStatuses);

            return orderService.createOrder(order);
        }).collect(Collectors.toList());
    }

    private void createBuyOrderPositions(List<Order> savedOrders) {
        savedOrders.stream()
                .filter(order -> order.getSide().equals(Side.BUY))
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
                    Transaction transaction = buildTransaction(order, transactionType, portfolio);

                    log.info("Saving transaction: {}", transaction);
                    return transactionService.createTransaction(transaction);
                }).toList();
        log.info("transactions successfully saved: {}", txs);
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

    private Transaction buildTransaction(Order order, TransactionType transactionType, Portfolio portfolio) {
        return Transaction.builder()
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
    }
}
