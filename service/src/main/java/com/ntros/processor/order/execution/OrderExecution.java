package com.ntros.processor.order.execution;

import com.ntros.model.order.Order;

import java.util.concurrent.CompletableFuture;

public interface OrderExecution {

    CompletableFuture<Order> executeOrder(Order order);

}
