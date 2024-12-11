package com.ntros.processor.order;

import com.ntros.converter.order.OrderConverter;
import com.ntros.dataservice.order.OrderService;
import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.dto.order.response.CreateOrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.model.order.Order;
import com.ntros.processor.order.execution.OrderExecution;
import com.ntros.processor.order.initialization.create.CreateOrderInitialization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Order Submission Workflow:
 * - when a user places an order:
 * - validate before saving:
 * - Buy order: ensure user has sufficient funds
 */
@Slf4j
@Service
public class CreateOrderProcessor extends AbstractOrderProcessor<CreateOrderRequest, CreateOrderResponse> {

    private final OrderExecution orderExecution;
    private final CreateOrderInitialization createOrderInitialization;
    private final OrderConverter orderConverter;


    @Autowired
    public CreateOrderProcessor(Executor executor, OrderService orderService, OrderExecution orderExecution,
                                CreateOrderInitialization createOrderInitialization,
                                OrderConverter orderConverter) {

        super(executor, orderService);
        this.orderExecution = orderExecution;
        this.createOrderInitialization = createOrderInitialization;
        this.orderConverter = orderConverter;

    }

    @Override
    protected CompletableFuture<Order> initialize(CreateOrderRequest orderRequest) {
        return createOrderInitialization.initializeOrder(orderRequest);
    }


    @Override
    protected CompletableFuture<Order> process(Order order) {
        return orderExecution.executeOrder(order);
    }


    @Override
    protected CompletableFuture<CreateOrderResponse> buildOrderResponse(Order order, Status status) {
        return supplyAsync(() -> {
            CreateOrderResponse createOrderResponse = new CreateOrderResponse();
            createOrderResponse.setStatus(status);
            createOrderResponse.setCreateOrderRequest(orderConverter.toDTO(order));
            return createOrderResponse;
        }, executor);
    }

}
