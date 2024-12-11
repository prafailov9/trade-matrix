package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;

import java.util.concurrent.CompletableFuture;

public interface CreateOrderInitializer {

    CompletableFuture<Order> initialize(CreateOrderRequest createOrderRequest);

}
