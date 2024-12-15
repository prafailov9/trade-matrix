package com.ntros.converter.order;


import com.ntros.converter.Converter;
import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class OrderProcessingConverter implements Converter<CreateOrderRequest, Order> {


    @Override
    public CreateOrderRequest toDTO(Order model) {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderType(model.getOrderType().getOrderTypeName());
        createOrderRequest.setPrice(model.getPrice());
        createOrderRequest.setQuantity(model.getQuantity());
        createOrderRequest.setAccountNumber(model.getWallet().getAccount().getAccountNumber());
        createOrderRequest.setCurrencyCode(model.getWallet().getCurrency().getCurrencyCode());
        createOrderRequest.setFilledQuantity(model.getFilledQuantity());
        createOrderRequest.setRemainingQuantity(model.getRemainingQuantity());
        createOrderRequest.setProductIsin(model.getMarketProduct().getProduct().getIsin());
        createOrderRequest.setTransactionType(model.getSide().name());

        return createOrderRequest;
    }

    @Override
    public Order toModel(CreateOrderRequest createOrderRequest) {
        return toModel(createOrderRequest, Order.builder());
    }

    public Order toModel(CreateOrderRequest orderRequest, Order.OrderBuilder orderBuilder) {
        Order order = orderBuilder
                .quantity(orderRequest.getQuantity())
                .filledQuantity(orderRequest.getFilledQuantity())
                .remainingQuantity(orderRequest.getRemainingQuantity())
                .price(orderRequest.getPrice())
                .placedAt(OffsetDateTime.now())
                .side(Side.valueOf(orderRequest.getTransactionType().toUpperCase()))
                .build();
        if (order.getFilledQuantity() == 0) { // new order
            order.setRemainingQuantity(order.getQuantity());
        }
        return order;
    }
}