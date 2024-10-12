package com.ntros.processor.order.execution;

import com.ntros.model.order.Order;

import java.util.concurrent.CompletableFuture;

public interface OrderExecutor {

    CompletableFuture<Order> execute(Order order);

}
