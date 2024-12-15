package com.ntros.converter.order;

import com.ntros.converter.Converter;
import com.ntros.dto.order.OrderDTO;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderType;
import org.springframework.stereotype.Component;

@Component
public class OrderDataConverter implements Converter<OrderDTO, Order> {
    @Override
    public OrderDTO toDTO(Order model) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderType(model.getOrderType().getOrderTypeName());
        dto.setPrice(model.getPrice());
        dto.setAccountNumber(model.getWallet().getAccount().getAccountNumber());
        dto.setCurrencyCode(model.getWallet().getCurrency().getCurrencyCode());
        dto.setQuantity(model.getQuantity());
        dto.setFilledQuantity(model.getFilledQuantity());
        dto.setRemainingQuantity(model.getRemainingQuantity());

        dto.setProductIsin(model.getMarketProduct().getProduct().getIsin());
        dto.setMarketCode(model.getMarketProduct().getMarket().getMarketCode());
        dto.setCurrentStatus(model.getOrderStatuses().get(model.getOrderStatuses().size() - 1).getCurrentStatus());
        dto.setTransactionType(model.getSide().name());

        return dto;
    }

    @Override
    public Order toModel(OrderDTO dto) {
        Order model = new Order();

        return null;
    }
}
