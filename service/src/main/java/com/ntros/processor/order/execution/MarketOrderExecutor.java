package com.ntros.processor.order.execution;

import com.ntros.dataservice.order.OrderService;
import com.ntros.dataservice.portfolio.PortfolioService;
import com.ntros.dataservice.position.PositionService;
import com.ntros.dataservice.wallet.WalletService;
import com.ntros.exception.RetryLimitExceededException;
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

import static java.util.concurrent.CompletableFuture.allOf;

/**
 * Immediately executes the order at best available price.
 */
@Service("market")
@Slf4j
public class MarketOrderExecutor extends AbstractOrderExecutor implements OrderExecutor {
    private static final int MAX_RETRIES = 5;
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

                            // exec the transaction asynchronously and track the transaction future
                            transactionFutures.add(executeOrderTransaction(incomingOrder, matchingOrder, matchedQuantity));
                        }
                    }

                    return remainingQuantity;
                }, executor)
                .thenComposeAsync(remainingQuantity ->
                        allOf(transactionFutures.toArray(new CompletableFuture[0]))
                                .thenApplyAsync(v -> {
                                    incomingOrder.setRemainingQuantity(remainingQuantity);
                                    return incomingOrder;
                                }));
    }

    @Override
    @Transactional
    @Modifying
    protected CompletableFuture<Void> executeOrderTransaction(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        return executeWithRetries(MAX_RETRIES,
                () -> updateFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity)
                        .thenComposeAsync(v -> saveMatchedOrders(incomingOrder, matchingOrder), executor)
                        .thenApplyAsync(this::createTransactions, executor)
                        .thenAcceptAsync(v -> log.info("All transactions executed successfully."), executor));
    }

    /**
     * Will try to execute supplied task. If an OptimisticLockException occurs, will retry
     * the same task MAX_RETRIES amount of times.
     * Any other exception will be propagated.
     *
     * @param retries - number of retries for acquiring the needed DB locks.
     * @param task    - current task to execute.
     * @return Void
     */
    private CompletableFuture<Void> executeWithRetries(int retries, Supplier<CompletableFuture<Void>> task) {
        return task.get().exceptionallyComposeAsync(ex -> {
            if (ex.getCause() instanceof OptimisticLockException && retries > 0) {
                log.error("Optimistic lock exception. Retrying... Remaining retries: {}", retries - 1);
                return executeWithRetries(retries - 1, task);
            } else {
                log.error("Retry limit exceeded for order execution: ", ex);
                throw new RetryLimitExceededException("Exceeded retry limit for executing order", ex);
            }
        });
    }

    protected CompletableFuture<Void> updateFundsAndAssets(Order incomingOrder, Order matchingOrder, int matchedQuantity) {
        incomingOrder.setFilledQuantity(incomingOrder.getFilledQuantity() + matchedQuantity);
        matchingOrder.setFilledQuantity(matchingOrder.getFilledQuantity() + matchedQuantity);
        incomingOrder.setRemainingQuantity(incomingOrder.getRemainingQuantity() - matchedQuantity);
        matchingOrder.setRemainingQuantity(matchingOrder.getRemainingQuantity() - matchedQuantity);

        return incomingOrder.getSide().equals(Side.BUY)
                ? transferFundsAndAssets(incomingOrder, matchingOrder, matchedQuantity)
                : transferFundsAndAssets(matchingOrder, incomingOrder, matchedQuantity);
    }

    private CompletableFuture<List<Order>> saveMatchedOrders(Order incoming, Order matching) {
        List<Order> matchedOrders = new ArrayList<>(List.of());
        return orderService.createOrder(incoming)
                .thenComposeAsync(incomingOrder -> {
                    matchedOrders.add(incomingOrder);
                    return orderService.createOrder(matching); // TODO: should update the matching order
                }, executor)
                .thenApplyAsync(matchingOrder -> {
                    matchedOrders.add(matchingOrder);
                    return matchedOrders;
                }, executor);
    }

    private CompletableFuture<Void> createTransactions(List<Order> savedOrders) {
        List<CompletableFuture<Transaction>> futureTransactions = savedOrders.stream()
                .map(order -> {
                    CompletableFuture<TransactionType> transactionTypeFuture =
                            transactionService.getTransactionType(order.getSide().name());
                    CompletableFuture<Portfolio> portfolioFuture =
                            portfolioService.getPortfolioByAccountProductIsin(order.getWallet().getAccount(), order.getProduct());

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

        return allOf(futureTransactions.toArray(new CompletableFuture[0]));
    }

}
