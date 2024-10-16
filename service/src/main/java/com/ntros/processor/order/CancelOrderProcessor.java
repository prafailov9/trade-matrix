package com.ntros.processor.order;

import com.ntros.dataservice.order.OrderService;
import com.ntros.dto.order.request.CancelOrderRequest;
import com.ntros.dto.order.response.CancelOrderResponse;
import com.ntros.dto.order.response.Status;
import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class CancelOrderProcessor extends AbstractOrderProcessor<CancelOrderRequest, CancelOrderResponse> {

    public CancelOrderProcessor(Executor executor, OrderService orderService) {

        super(executor, orderService);
    }

    @Override
    protected CompletableFuture<Order> initialize(CancelOrderRequest orderRequest) {
        // find orer by isin + an
        return orderService.getOrder(orderRequest.getAccountNumber(), orderRequest.getProductIsin());
    }

    @Override
    protected CompletableFuture<Order> process(Order order) {
        // if none found, throw excetion, else update estatus
        return orderService.updateOrderStatus(order, CurrentOrderStatus.CANCELLED)
                .thenApplyAsync(orderStatus -> {
                    order.getOrderStatusList().add(orderStatus);
                    return order;
                }, executor);
    }


    @Override
    protected CompletableFuture<CancelOrderResponse> buildCreateOrderResponse(Order order, Status status) {
        return null;
    }
}
