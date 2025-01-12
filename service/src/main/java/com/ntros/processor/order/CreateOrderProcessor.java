package com.ntros.processor.order;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.dto.order.response.CreateOrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.model.order.Order;
import com.ntros.processor.order.execution.OrderExecution;
import com.ntros.processor.order.initialization.create.CreateOrderInitialization;
import com.ntros.processor.order.notification.CallbackNotifier;
import com.ntros.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.lang.String.format;

@Slf4j
@Service
public class CreateOrderProcessor extends AbstractOrderProcessor<CreateOrderRequest, CreateOrderResponse> {

    private final OrderExecution orderExecution;
    private final CreateOrderInitialization createOrderInitialization;

    @Autowired
    public CreateOrderProcessor(Executor executor,
                                OrderService orderService,
                                CallbackNotifier<CreateOrderResponse> orderCallbackNotifier,
                                OrderExecution orderExecution,
                                CreateOrderInitialization createOrderInitialization) {

        super(executor, orderService, orderCallbackNotifier);

        this.orderExecution = orderExecution;
        this.createOrderInitialization = createOrderInitialization;
    }

    @Override
    protected Order initialize(CreateOrderRequest orderRequest) {
        return createOrderInitialization.initializeOrder(orderRequest);
    }

    @Override
    protected CompletableFuture<Order> process(Order order) {
        return orderExecution.executeOrder(order);
    }

    @Override
    protected CreateOrderResponse buildOrderSuccessResponse(Order order) {
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setStatus(Status.SUCCESS);
        createOrderResponse.setMessage(format("Order [%s] successfully initialized and scheduled for fulfillment.", order));
        createOrderResponse.setName(format("%s_%s_%s", order.getWallet().getAccount().getAccountNumber(),
                order.getMarketProduct().getProduct().getProductName(), order.getMarketProduct().getMarket().getMarketCode()));

        return createOrderResponse;
    }

    @Override
    protected CreateOrderResponse buildOrderFailedResponse(Throwable ex) {
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setStatus(Status.FAILURE);
        createOrderResponse.setMessage(ex.getMessage());
        return createOrderResponse;
    }
}
