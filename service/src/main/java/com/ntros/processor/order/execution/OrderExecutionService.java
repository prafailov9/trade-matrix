package com.ntros.processor.order.execution;

import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class OrderExecutionService implements OrderExecution {

    private final Executor executor;

    private final Map<String, OrderExecutor> orderExecutors;


    @Autowired
    public OrderExecutionService(@Qualifier("taskExecutor") Executor executor, Map<String, OrderExecutor> orderExecutors) {
        this.executor = executor;
        this.orderExecutors = orderExecutors;
    }

    public CompletableFuture<Order> executeOrder(Order order) {
        return getOrderExecutor(order.getOrderType().getOrderTypeName().toLowerCase())
                .thenComposeAsync(orderExecutor ->
                        orderExecutor.execute(order), executor);
    }

    private CompletableFuture<OrderExecutor> getOrderExecutor(String orderType) {
        return supplyAsync(() -> {
            OrderExecutor orderExecutor = orderExecutors.get(orderType);
            if (orderExecutor == null) {
                throw new IllegalArgumentException("Unknown order type: " + orderType);
            }
            return orderExecutor;
        }, executor);
    }


}
