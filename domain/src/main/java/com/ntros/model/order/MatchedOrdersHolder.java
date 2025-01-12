package com.ntros.model.order;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchedOrdersHolder {

    private final Order incomingOrder;

    private final List<Order> matchingOrders;

    private final List<Order> allOrders;

    private MatchedOrdersHolder(Order incomingOrder, List<Order> matchingOrders, List<Order> allOrders) {
        this.incomingOrder = incomingOrder;
        this.matchingOrders = matchingOrders;
        this.allOrders = allOrders;
    }


    public static MatchedOrdersHolder of(Order incomingOrder, List<Order> matchingOrders) {
        if (incomingOrder == null) {
            throw new IllegalArgumentException("Cannot build Matching order pair with empty incoming order.");
        }
        if (matchingOrders == null || matchingOrders.isEmpty()) {
            throw new IllegalArgumentException("Cannot build Matching order pair with empty matching orders.");
        }
        List<Order> allOrders = new ArrayList<>(List.copyOf(matchingOrders));
        allOrders.add(incomingOrder);

        return new MatchedOrdersHolder(incomingOrder, matchingOrders, allOrders);
    }

}
