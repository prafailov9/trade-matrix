package com.ntros.processor.order.execution;

import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrderExecutionService implements OrderExecution {

    private final Map<String, OrderExecutor> orderExecutors;


    @Autowired
    public OrderExecutionService(Map<String, OrderExecutor> orderExecutors) {
        this.orderExecutors = orderExecutors;
    }

    public CompletableFuture<Order> executeOrder(Order order) {
        String orderType = order.getOrderType().getOrderTypeName().toLowerCase();
        OrderExecutor orderExecutor = orderExecutors.get(orderType);
        if (orderExecutor == null) {
            throw new IllegalArgumentException("Unknown order type: " + orderType);
        }
        return orderExecutor.execute(order);
    }

}
