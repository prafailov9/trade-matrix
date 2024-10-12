package com.ntros.dataservice.order;

import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.model.order.OrderType;
import com.ntros.model.product.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrderService {

    // used for opt locking
    CompletableFuture<Order> findOrderById(Integer orderId);

    CompletableFuture<Order> createOrder(Order order);

    CompletableFuture<Order> getOrder(String accountNumber, String productIsin);

    CompletableFuture<Void> updateOrder(Order order);

    CompletableFuture<List<Order>> getAllOrders();

    CompletableFuture<OrderType> getOrderType(String type);

    CompletableFuture<String> deleteAllOrders();

    CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order);

    CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order, CurrentOrderStatus status);

    CompletableFuture<OrderStatus> updateOrderStatus(Order order, CurrentOrderStatus orderStatus);

    CompletableFuture<OrderStatus> determineAndUpdateCurrentStatus(Order order);

    /**
     * Order Matching Logic
     */

    CompletableFuture<List<Order>> findMatchingOrders(Order order);

    CompletableFuture<List<Order>> findMatchingBuyOrders(Product product, BigDecimal price);

    CompletableFuture<List<Order>> findMatchingSellOrders(Product product, BigDecimal price);
}
