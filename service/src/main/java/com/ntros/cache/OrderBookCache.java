package com.ntros.cache;

import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import static com.ntros.model.order.Side.BUY;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Slf4j
public class OrderBookCache implements OrderCache {

    private static final int INITIAL_QUEUE_CAPACITY = 10;

    private final String marketCode;
    /**
     * Lock for updating price levels securely
     */
    private final ReentrantLock lock;
    /**
     * Stores all buy prices -> asset quantities for the orders
     */
    private final PriorityBlockingQueue<PriceLevel> bids;

    /**
     * Stores all sell prices -> asset quantities for the orders
     */
    private final PriorityBlockingQueue<PriceLevel> asks;

    /**
     * Map to store all OPEN orders
     */
    private final Map<Integer, Order> orderMap;

    /**
     * Map to group Orders by exact Price
     */
    private final Map<BigDecimal, List<Order>> priceMap;


    private OrderBookCache(String marketCode) {
        this.marketCode = marketCode;

        // DESC order for bids, ASC order for asks
        this.bids = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, (o1, o2) -> o2.price.compareTo(o1.price));
        this.asks = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, comparing(o -> o.price));
        this.orderMap = new ConcurrentHashMap<>();
        this.priceMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    // Init-On-Demand Singleton to handle instances for each market
    private static class InstanceHolder {
        private static final ConcurrentHashMap<String, OrderBookCache> INSTANCES;

        static {
            INSTANCES = new ConcurrentHashMap<>();
            // populate with supported markets
            INSTANCES.put("NYSE", new OrderBookCache("NYSE"));
            INSTANCES.put("NASDAQ", new OrderBookCache("NASDAQ"));
            INSTANCES.put("TSX", new OrderBookCache("TSX"));
            INSTANCES.put("BMV", new OrderBookCache("BMV"));
            INSTANCES.put("LSE", new OrderBookCache("LSE"));
        }
    }

    public static OrderBookCache getInstance(String marketCode) {
        return InstanceHolder.INSTANCES.get(marketCode);
        // thread-safe lazy init is ensured here
//        return InstanceHolder.INSTANCES.computeIfAbsent(marketCode, OrderBookCache::new);
    }

    @Override
    public void put(Integer id, Order order) {
        validateOrder(id, order);

        // add order to price map
        priceMap.compute(order.getPrice(), (price, ordersAtPriceList) -> {
            if (ordersAtPriceList == null) {
                ordersAtPriceList = Collections.synchronizedList(new ArrayList<>());
            }
            ordersAtPriceList.add(order);
            return ordersAtPriceList;
        });
        // add to order map
        orderMap.put(id, order);

        // update price level in correct heap
        lock.lock();
        try {
            updatePriceLevel(order.getPrice(), order.getQuantity(), order.getSide());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Order> remove(Integer key) {
        Order cachedOrder = orderMap.remove(key);
        if (cachedOrder == null) {
            String err = format("Order for ID: %s not found.", key);
            log.error(err);
            throw new NoSuchElementException(err);
        }

        BigDecimal cachedPrice = cachedOrder.getPrice();

        List<Order> ordersAtPriceList = priceMap.get(cachedPrice);
        if (ordersAtPriceList == null) {
            log.info("Order with ID: {} removed from cache.", cachedOrder.getOrderId());
            return Optional.of(cachedOrder);
        }
        // remove order from prices map
        synchronized (ordersAtPriceList) {
            ordersAtPriceList.remove(cachedOrder);
            if (ordersAtPriceList.isEmpty()) {
                priceMap.remove(cachedPrice);
                // remove price from heap
                lock.lock();
                if (cachedOrder.getSide().equals(BUY)) {
                    bids.removeIf(priceEntry -> priceEntry.price.compareTo(cachedPrice) == 0);
                } else {
                    asks.removeIf(priceEntry -> priceEntry.price.compareTo(cachedPrice) == 0);
                }
            }
        }
        return Optional.of(cachedOrder);
    }

    @Override
    public Optional<Order> get(Integer key) {
        return ofNullable(orderMap.get(key));
    }

    @Override
    public BigDecimal getBestBidPrice() {
        PriceLevel bid = bids.peek();
        if (bid == null || bid.price == null) {
            log.info("No active bid prices.");
            return BigDecimal.ZERO;
        }
        return bid.price;
    }

    @Override
    public BigDecimal getBestAskPrice() {
        PriceLevel ask = asks.peek();
        if (ask == null || ask.price == null) {
            log.info("No active ask prices.");
            return BigDecimal.ZERO;
        }
        return ask.price;
    }

    @Override
    public String getMarketCode() {
        return marketCode;
    }

    private void updatePriceLevel(BigDecimal price, int orderQuantity, Side side) {
        int totalQuantity = calculateTotalQuantity(price);

        PriceLevel priceLevel = new PriceLevel(price, totalQuantity + orderQuantity);
        if (side.equals(BUY)) {
            bids.removeIf(priceEntry -> priceEntry.price.compareTo(price) == 0);
            bids.add(priceLevel);
        } else {
            asks.removeIf(priceEntry -> priceEntry.price.compareTo(price) == 0);
            asks.add(priceLevel);
        }
    }

    private int calculateTotalQuantity(BigDecimal price) {
        List<Order> ordersAtPriceList = priceMap.get(price);
        if (ordersAtPriceList == null || ordersAtPriceList.isEmpty()) {
            return 0;
        }
        synchronized (ordersAtPriceList) {
            return ordersAtPriceList.stream()
                    .mapToInt(Order::getQuantity)
                    .sum();
        }
    }

    private void validateOrder(Integer id, Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(format("Invalid order ID: %s", id));
        }
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(format("Invalid order price: %s", order.getPrice()));
        }
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException(format("Invalid order asset quantity: %s", order.getQuantity()));
        }
    }

    private static class PriceLevel {
        BigDecimal price;
        Integer quantity;

        PriceLevel(BigDecimal price, Integer quantity) {
            this.price = price;
            this.quantity = quantity;
        }
    }

}
