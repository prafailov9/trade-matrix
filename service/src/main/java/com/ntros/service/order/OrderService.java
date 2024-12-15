package com.ntros.service.order;

import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.model.order.OrderType;
import com.ntros.model.product.MarketProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrderService {

    Order getOrder(String accountNumber, String productIsin);
    Order createOrder(Order order);
    Order updateOrder(Order order);
    CompletableFuture<List<Order>> getAllOrders();
    OrderType getOrderType(String type);
    CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order);

    OrderStatus updateOrderStatus(Order order, CurrentOrderStatus orderStatus);
    OrderStatus determineAndUpdateCurrentStatus(Order order);

    /**
     * Order Matching Logic
     */
    List<Order> findMatchingOrders(Order order);
    List<Order> findMatchingBuyOrders(MarketProduct marketProduct, BigDecimal price);
    List<Order> findMatchingSellOrders(MarketProduct marketProduct, BigDecimal price);
}
