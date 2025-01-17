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
    Order updateOrder(Integer orderId, Order order);
    CompletableFuture<List<Order>> getAllOrders();
    CompletableFuture<List<Order>> getAllOpenOrders();
    CompletableFuture<List<Order>> getAllFilledOrders();
    CompletableFuture<List<Order>> getAllPartialOrders();
    OrderType getOrderType(String type);
    CompletableFuture<List<OrderStatus>> getAllByOrder(Order order);

    OrderStatus updateOrderStatus(Order order, CurrentOrderStatus orderStatus);
    OrderStatus determineAndUpdateCurrentStatus(Order order);

    void transferFunds(Order buyOrder, Order sellOrder, int matchedQuantity);

    /**
     * Order Matching Logic
     */
    List<Order> findMatchingOrders(Order order);
}
