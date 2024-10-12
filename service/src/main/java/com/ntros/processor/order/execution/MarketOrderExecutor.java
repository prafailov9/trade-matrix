package com.ntros.processor.order.execution;

import com.ntros.dataservice.order.OrderService;
import com.ntros.dataservice.portfolio.PortfolioService;
import com.ntros.dataservice.position.PositionService;
import com.ntros.dataservice.wallet.WalletService;
import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.transaction.Transaction;
import com.ntros.model.transaction.TransactionType;
import com.ntros.processor.transaction.TransactionService;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Immediately executes order under current market conditions.
 */
@Service("market")
@Slf4j
public class MarketOrderExecutor extends AbstractOrderExecutor implements OrderExecutor {
    private static final int MAX_RETRIES = 3;

    private final PortfolioService portfolioService;

    public MarketOrderExecutor(Executor executor, OrderService orderService, TransactionService transactionService,
                               PositionService positionService, WalletService walletService, PortfolioService portfolioService) {
        super(executor, orderService, transactionService, positionService, walletService);
        this.portfolioService = portfolioService;
    }

    @Override
    protected CompletableFuture<Order> fulfillOrders(Order incomingOrder, List<Order> matchingOrders) {
        List<CompletableFuture<Void>> transactionFutures = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> {
            int remainingQuantity = incomingOrder.getQuantity();

            for (Order matchingOrder : matchingOrders) {
                if (remainingQuantity > 0) {
                    int matchedQuantity = Math.min(remainingQuantity, matchingOrder.getQuantity());
                    remainingQuantity -= matchedQuantity;

                    // Step 5: Execute the transaction asynchronously and track the transaction future
                    transactionFutures.add(executeOrderTransaction(incomingOrder, matchingOrder, matchedQuantity));
                }
            }

            return remainingQuantity;
        }, executor).thenComposeAsync(remainingQuantity ->
                CompletableFuture.allOf(transactionFutures.toArray(new CompletableFuture[0]))
                        .thenApplyAsync(v -> {
                            incomingOrder.setRemainingQuantity(remainingQuantity);
                            return incomingOrder;
                        }));
    }

    @Override
    @Transactional
    @Modifying
    protected CompletableFuture<Void> executeOrderTransaction(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        return executeWithRetries(() -> adjustBalances(incomingOrder, matchingOrder, matchedQuantity)
                        .thenAcceptAsync(v -> updateOrderQuantities(incomingOrder, matchingOrder, matchedQuantity), executor)
                        .thenComposeAsync(v -> saveMatchedOrders(incomingOrder, matchingOrder), executor)
                        .thenApplyAsync(this::createTransactions, executor)
                        .thenAcceptAsync(v -> log.info("All transactions executed successfully."), executor)
                , MAX_RETRIES);
    }

    private CompletableFuture<Void> executeWithRetries(Supplier<CompletableFuture<Void>> task, int retries) {
        return task.get().exceptionallyComposeAsync(ex -> {
            if (ex.getCause() instanceof OptimisticLockException && retries > 0) {
                log.error("Optimistic lock exception. Retrying... Remaining retries: {}", retries - 1);
                return executeWithRetries(task, retries - 1);
            } else {
                throw new RuntimeException("Exceeded retry limit for optimistic lock", ex);
            }
        });
    }

    protected CompletableFuture<Void> adjustBalances(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        return incomingOrder.getSide().equals(Side.BUY)
                ? adjustWalletBalances(incomingOrder, matchingOrder, matchedQuantity)
                : adjustWalletBalances(matchingOrder, incomingOrder, matchedQuantity);
    }

    private void updateOrderQuantities(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        incomingOrder.setFilledQuantity(incomingOrder.getFilledQuantity() + matchedQuantity);
        matchingOrder.setFilledQuantity(matchingOrder.getFilledQuantity() + matchedQuantity);

        incomingOrder.setRemainingQuantity(incomingOrder.getRemainingQuantity() - matchedQuantity);
        matchingOrder.setRemainingQuantity(matchingOrder.getRemainingQuantity() - matchedQuantity);
    }

    private CompletableFuture<List<Order>> saveMatchedOrders(Order incoming, Order matching) {
        List<Order> executedOrders = new ArrayList<>(List.of());
        return orderService.createOrder(incoming)
                .thenComposeAsync(order -> {
                    executedOrders.add(order);
                    return orderService.createOrder(matching);
                }, executor)
                .thenApplyAsync(order -> {
                    executedOrders.add(order);
                    return executedOrders;
                }, executor);
    }

    private CompletableFuture<Void> createTransactions(List<Order> executedOrders) {
        List<CompletableFuture<Transaction>> futureTransactions = executedOrders.stream()
                .map(order -> {
                    CompletableFuture<TransactionType> transactionTypeFuture = transactionService.getTransactionType(order.getSide().name());
                    CompletableFuture<Portfolio> portfolioFuture = portfolioService.getPortfolioByAccountProductIsin(order.getWallet().getAccount(), order.getProduct());

                    return transactionTypeFuture.thenCombineAsync(portfolioFuture, (transactionType, portfolio) -> Transaction.builder()
                                    .order(order)
                                    .product(order.getProduct())
                                    .wallet(order.getWallet())
                                    .currency(order.getWallet().getCurrency().getCurrencyCode())
                                    .price(order.getPrice())
                                    .quantity(order.getQuantity())
                                    .transactionType(transactionType)
                                    .portfolio(portfolio)
                                    .build())
                            .thenComposeAsync(transactionService::createTransaction);
                })
                .toList();

        return CompletableFuture.allOf(futureTransactions.toArray(new CompletableFuture[0]));
    }

}
