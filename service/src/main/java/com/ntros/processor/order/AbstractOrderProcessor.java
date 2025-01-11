package com.ntros.processor.order;

import com.ntros.dto.order.request.OrderRequest;
import com.ntros.dto.order.response.OrderResponse;
import com.ntros.model.order.Order;
import com.ntros.processor.order.notification.Notifier;
import com.ntros.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.runAsync;

@Service
@Slf4j
public abstract class AbstractOrderProcessor<S extends OrderRequest, R extends OrderResponse> implements OrderProcessor<S, R> {

    protected final Executor executor;
    protected final OrderService orderService;
    protected final Notifier<Order> orderNotifier;

    @Autowired
    public AbstractOrderProcessor(@Qualifier("taskExecutor") Executor executor,
                                  OrderService orderService,
                                  Notifier<Order> orderNotifier) {

        this.executor = executor;
        this.orderService = orderService;
        this.orderNotifier = orderNotifier;
    }

    /**
     * Successfully placed(initialized) orders should be returned immediately. OrderFulfillmentShould happen in the background.
     * Process incoming order request.
     * - Initialize order, perform validations. Once initialized, it will be placed with OPEN status.
     * - After that, order fulfillment begins. It will be fulfilled based on its directive(BUY/SELL)
     * and type(MARKET, LIMIT, STOP)
     * - Build response
     *
     * @param orderRequest - incoming order
     * @return Order Response
     * <p>
     */
    @Override
    public R processOrder(S orderRequest) {
        try {
            Order initializedOrder = initialize(orderRequest);
            runAsync(() -> {
                try {
                    Order processedOrder = process(initializedOrder).join();
                    log.info("Successfully processed order: [{}]", processedOrder);

                    orderNotifier.notify(processedOrder, orderRequest.getCallbackUrl());
                } catch (Exception ex) {
                    log.error("Failed to process order with id: {}", initializedOrder.getOrderId(), ex);
                }
            }, executor);
            return buildOrderSuccessResponse(initializedOrder);
        } catch (Exception ex) {
            log.error("Failed to initialize order {}. Error:", orderRequest, ex);
            return buildOrderFailedResponse(ex);
        }
    }

    protected abstract Order initialize(S orderRequest);

    protected abstract CompletableFuture<Order> process(Order order);

    protected abstract R buildOrderSuccessResponse(Order order);

    protected abstract R buildOrderFailedResponse(Throwable ex);


}
