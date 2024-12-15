package com.ntros.processor.order;

import com.ntros.service.order.OrderService;
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

    /**
     * Successfully placed(initialized) orders should be returned immediately. OrderFulfillmentShould happen in the background.
     * Process incoming order request.
     * - Initialize order, perform validations. Once initialized, it will be placed with OPEN status.
     * - After that, order fulfillment begins. It will be fulfilled based on its directive(BUY/SELL)
     *      and type(MARKET, LIMIT, STOP)
     * - Build response
     * @param orderRequest - incoming order
     * @return Order Response
     *
     * TODO: Detach init and fulfillment(processing). After Init, return response immediately, process order in background.
     */
    @Override
    public CompletableFuture<R> processOrder(T orderRequest) {
        return initialize(orderRequest)
                .thenComposeAsync(this::process, executor)
                .thenComposeAsync(this::buildOrderSuccessResponse, executor)
                .exceptionally(ex -> {
                    log.error("Failed to process order {}. Error:", orderRequest, ex.getCause());
                    return buildOrderFailedResponse(ex.getCause());
                });
    }

    protected abstract CompletableFuture<Order> initialize(T orderRequest);

    protected abstract CompletableFuture<Order> process(Order order);

    protected abstract CompletableFuture<R> buildOrderSuccessResponse(Order order);

    protected abstract R buildOrderFailedResponse(Throwable ex);


}
