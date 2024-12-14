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
     * TODO: Detach init and processing. After Init, a response should be returned immediately to the user.
     */
    @Override
    public CompletableFuture<R> processOrder(T orderRequest) {
        return initialize(orderRequest)
                .thenComposeAsync(this::process, executor)
                .thenComposeAsync(processedOrder -> buildOrderResponse(processedOrder, Status.SUCCESS))
                .exceptionally(ex -> {
                    log.error("Failed to process order {}. Message: {}", orderRequest, ex.getMessage());
                    throw new OrderProcessingException(ex.getMessage(), ex.getCause());
                });
    }

    protected abstract CompletableFuture<Order> initialize(T orderRequest);

    protected abstract CompletableFuture<Order> process(Order order);

    protected abstract CompletableFuture<R> buildOrderResponse(Order order, Status status);


}
