package com.ntros.cache;

import com.ntros.model.order.Order;
import com.ntros.model.order.Side;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderCache {

    void addOrder(Order order);

    Optional<Order> getOrder(Integer orderId);

    Optional<Order> removeOrder(Integer orderId);

    List<Order> getMatchingOrders(BigDecimal price, String isin, Side side, String orderType);

    String getMarket();

    int size();

    void clear();

}
