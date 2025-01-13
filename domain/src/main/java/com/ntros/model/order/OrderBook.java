package com.ntros.model.order;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class OrderBook {
    private final List<Order> orders = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static class OrderBookInstance {

    }

    public void addOrder(Order order) {
        lock.writeLock().lock();
        try {
            orders.add(order);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeOrder(Order order) {
        lock.writeLock().lock();
        try {
            orders.remove(order);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Order> getMatchingOrders(Order incomingOrder) {
        lock.readLock().lock();
        try {
            // Retrieve potential matches based on price and side
            return orders.stream()
                    .filter(existingOrder -> isPotentialMatch(incomingOrder, existingOrder))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isPotentialMatch(Order incomingOrder, Order existingOrder) {
        return incomingOrder.getSide().equals(Side.BUY)
                ? existingOrder.getSide().equals(Side.SELL) && existingOrder.getPrice().compareTo(incomingOrder.getPrice()) <= 0
                : existingOrder.getSide().equals(Side.BUY) && existingOrder.getPrice().compareTo(incomingOrder.getPrice()) >= 0;
    }
}
