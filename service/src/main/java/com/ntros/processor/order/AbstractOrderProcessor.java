package com.ntros.processor.order;

import com.ntros.dataservice.order.OrderService;
import com.ntros.dto.order.request.OrderRequest;
import com.ntros.dto.order.response.OrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.exception.OrderProcessingException;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public abstract class AbstractOrderProcessor<T extends OrderRequest, R extends OrderResponse> implements OrderProcessor<T, R> {

    protected final Executor executor;
    protected final OrderService orderService;

    @Autowired
    public AbstractOrderProcessor(@Qualifier("taskExecutor") Executor executor,
                                  OrderService orderService) {

        this.executor = executor;
        this.orderService = orderService;
    }

    @Override
    public CompletableFuture<R> processOrder(T orderRequest) {
        // initialize order, perform validations
        // fulfill order directive(buy/sell)
        // build response
        return initialize(orderRequest)
                .thenComposeAsync(initializedOrder -> process(initializedOrder)
                        .thenComposeAsync(finalizedOrder -> buildCreateOrderResponse(finalizedOrder, Status.SUCCESS)))
                .exceptionally(ex -> {
                    log.error("Failed to process order {}. Message: {}", orderRequest, ex.getMessage());
                    throw new OrderProcessingException(ex.getMessage(), ex.getCause());
                });
    }

    protected abstract CompletableFuture<Order> initialize(T orderRequest);

    protected abstract CompletableFuture<Order> process(Order order);

    protected abstract CompletableFuture<R> buildCreateOrderResponse(Order order, Status status);


}
