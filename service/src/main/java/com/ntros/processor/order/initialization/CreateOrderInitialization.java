package com.ntros.processor.order.initialization;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;

import java.util.concurrent.CompletableFuture;

public interface CreateOrderInitialization {

    CompletableFuture<Order> initializeOrder(CreateOrderRequest createOrderRequest);

}
