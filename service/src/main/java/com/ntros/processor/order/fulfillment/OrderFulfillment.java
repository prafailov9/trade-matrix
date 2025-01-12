package com.ntros.processor.order.fulfillment;

import com.ntros.model.order.Order;
import com.ntros.model.order.MatchedOrdersHolder;

import java.util.List;

public interface OrderFulfillment {

    MatchedOrdersHolder fulfillOrders(Order incomingOrder, List<Order> matchingOrders);

}
