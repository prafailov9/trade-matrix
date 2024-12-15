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

    // used for opt locking
    CompletableFuture<Order> findOrderByIdAsync(Integer orderId);

    CompletableFuture<Order> createOrderAsync(Order order);
    Order createOrder(Order order);

    CompletableFuture<Order> getOrderAsync(String accountNumber, String productIsin);

    CompletableFuture<Void> updateOrderAsync(Order order);

    CompletableFuture<List<Order>> getAllOrdersAsync();

    CompletableFuture<OrderType> getOrderTypeAsync(String type);
    OrderType getOrderType(String type);

    CompletableFuture<String> deleteAllOrdersAsync();

    CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order);

    CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order, CurrentOrderStatus status);

    CompletableFuture<OrderStatus> updateOrderStatusAsync(Order order, CurrentOrderStatus orderStatus);
    OrderStatus updateOrderStatus(Order order, CurrentOrderStatus orderStatus);

    CompletableFuture<OrderStatus> determineAndUpdateCurrentStatusAsync(Order order);
    OrderStatus determineAndUpdateCurrentStatus(Order order);

    /**
     * Order Matching Logic
     */

    CompletableFuture<List<Order>> findMatchingOrdersAsync(Order order);
    List<Order> findMatchingOrders(Order order);

    CompletableFuture<List<Order>> findMatchingBuyOrdersAsync(MarketProduct marketProduct, BigDecimal price);
    List<Order> findMatchingBuyOrders(MarketProduct marketProduct, BigDecimal price);

    CompletableFuture<List<Order>> findMatchingSellOrdersAsync(MarketProduct marketProduct, BigDecimal price);
    List<Order> findMatchingSellOrders(MarketProduct marketProduct, BigDecimal price);
}
