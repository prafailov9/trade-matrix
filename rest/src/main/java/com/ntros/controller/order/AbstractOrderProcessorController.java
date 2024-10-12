package com.ntros.controller.order;

import com.ntros.controller.AbstractApiController;
import com.ntros.dto.order.request.OrderRequest;
import com.ntros.dto.order.response.OrderResponse;
import com.ntros.processor.order.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
public abstract class AbstractOrderProcessorController<T extends OrderRequest, R extends OrderResponse> extends AbstractApiController implements OrderProcessorController<T> {

    protected final Executor executor;
    protected final OrderProcessor<T, R> orderProcessor;

    @Autowired
    public AbstractOrderProcessorController(@Qualifier("taskExecutor") Executor executor, OrderProcessor<T, R> orderProcessor) {
        this.executor = executor;
        this.orderProcessor = orderProcessor;
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> process(T orderRequest) {
        return orderProcessor.processOrder(orderRequest)
                .handleAsync((this::handleResponseAsync), executor);
    }


}
