package com.ntros.processor.order;

import com.ntros.converter.order.OrderDataConverter;
import com.ntros.service.order.OrderService;
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

@Slf4j
@Service
public class CreateOrderProcessor extends AbstractOrderProcessor<CreateOrderRequest, CreateOrderResponse> {

    private final OrderExecution orderExecution;
    private final CreateOrderInitialization createOrderInitialization;

    private final OrderDataConverter orderDataConverter;

    @Autowired
    public CreateOrderProcessor(Executor executor, OrderService orderService, OrderExecution orderExecution,
                                CreateOrderInitialization createOrderInitialization,
                                OrderDataConverter orderDataConverter) {

        super(executor, orderService);
        this.orderExecution = orderExecution;
        this.createOrderInitialization = createOrderInitialization;
        this.orderDataConverter = orderDataConverter;

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
    protected CompletableFuture<CreateOrderResponse> buildOrderSuccessResponse(Order order) {
        return supplyAsync(() -> {
            CreateOrderResponse createOrderResponse = new CreateOrderResponse();
            createOrderResponse.setStatus(Status.SUCCESS);
            createOrderResponse.setOrderDTO(orderDataConverter.toDTO(order));
            return createOrderResponse;
        }, executor);
    }

    @Override
    protected CreateOrderResponse buildOrderFailedResponse(Throwable ex) {
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setStatus(Status.FAILURE);
        createOrderResponse.setMessage(ex.getMessage());
        return createOrderResponse;
    }

}
