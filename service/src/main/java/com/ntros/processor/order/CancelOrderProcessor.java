package com.ntros.processor.order;

import com.ntros.model.order.OrderStatus;
import com.ntros.service.order.OrderService;
import com.ntros.dto.order.request.CancelOrderRequest;
import com.ntros.dto.order.response.CancelOrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class CancelOrderProcessor extends AbstractOrderProcessor<CancelOrderRequest, CancelOrderResponse> {

    public CancelOrderProcessor(Executor executor, OrderService orderService) {
        super(executor, orderService);
    }

    @Override
    protected CompletableFuture<Order> initialize(CancelOrderRequest orderRequest) {
        return supplyAsync(() -> orderService.getOrder(orderRequest.getAccountNumber(), orderRequest.getProductIsin()));
    }

    @Override
    protected CompletableFuture<Order> process(Order order) {
        return supplyAsync(() -> {
            OrderStatus orderStatus = orderService.updateOrderStatus(order, CurrentOrderStatus.CANCELLED);
            if (order.getOrderStatuses() == null) {
                order.setOrderStatuses(new ArrayList<>());
            }
            order.getOrderStatuses().add(orderStatus);
            return orderService.updateOrder(order);
        }, executor);
    }

    @Override
    protected CompletableFuture<CancelOrderResponse> buildOrderSuccessResponse(Order order) {
        return supplyAsync(() -> {
            CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
            cancelOrderResponse.setMessage(String.format("Order for product:{%s} successfully canceled", order.getMarketProduct().getProduct()));
            cancelOrderResponse.setStatus(Status.SUCCESS);
            return cancelOrderResponse;
        }, executor);
    }

    @Override
    protected CancelOrderResponse buildOrderFailedResponse(Throwable ex) {
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        cancelOrderResponse.setMessage(String.format("Failed to process cancellation: %s", ex.getMessage()));
        cancelOrderResponse.setStatus(Status.FAILURE);
        return cancelOrderResponse;
    }
}
