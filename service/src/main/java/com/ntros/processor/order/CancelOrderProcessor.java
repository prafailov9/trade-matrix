package com.ntros.processor.order;

import com.ntros.dto.order.request.CancelOrderRequest;
import com.ntros.dto.order.response.CancelOrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.processor.order.notification.Notifier;
import com.ntros.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.ntros.dto.order.response.Status.SUCCESS;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class CancelOrderProcessor extends AbstractOrderProcessor<CancelOrderRequest, CancelOrderResponse> {

    public CancelOrderProcessor(Executor executor, OrderService orderService, Notifier<Order> orderNotifier) {
        super(executor, orderService, orderNotifier);
    }

    @Override
    protected Order initialize(CancelOrderRequest orderRequest) {
        return orderService.getOrder(orderRequest.getAccountNumber(), orderRequest.getProductIsin());
    }

    @Override
    protected CompletableFuture<Order> process(Order order) {
        return supplyAsync(() -> {
            OrderStatus orderStatus = orderService.updateOrderStatus(order, CurrentOrderStatus.CANCELLED);
            if (order.getOrderStatuses() == null) {
                order.setOrderStatuses(new ArrayList<>());
            }
            order.getOrderStatuses().add(orderStatus);
            return orderService.updateOrder(order.getOrderId(), order);
        }, executor);
    }

    @Override
    protected CancelOrderResponse buildOrderSuccessResponse(Order order) {
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();

        cancelOrderResponse
                .setMessage(
                        format("Order for product:{%s} successfully canceled",
                                order.getMarketProduct().getProduct()));
        cancelOrderResponse.setStatus(SUCCESS);
        log.info("Successfully cancelled order: {}", order);

        return cancelOrderResponse;
    }

    @Override
    protected CancelOrderResponse buildOrderFailedResponse(Throwable ex) {
        String err = format("Failed to process cancellation: %s", ex.getMessage());

        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        cancelOrderResponse.setMessage(err);
        cancelOrderResponse.setStatus(Status.FAILURE);

        log.error(format("%s. Sending response: {}", err), cancelOrderResponse, ex);

        return cancelOrderResponse;
    }
}
