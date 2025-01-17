package com.ntros.cache;

import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.ntros.model.order.Side.BUY;

/**
 * In-Memory Cache for OPEN, PARTIALLY_FILLED orders. One instance per Market.
 */
@Slf4j
public class OrderBook implements OrderCache {

    private static final String MARKET_ORDER = "MARKET";
    private static final String LIMIT_ORDER = "LIMIT";

    private final String market;

    // Primary index: ISIN -> Secondary index (price -> orders)
    private final Map<String, TreeMap<BigDecimal, List<Order>>> bids;
    private final Map<String, TreeMap<BigDecimal, List<Order>>> asks;

    private final Map<Integer, Order> orders;

    private final ReentrantLock bidsLock;
    private final ReentrantLock asksLock;

    private OrderBook(String market) {
        this.market = market;

        bidsLock = new ReentrantLock();
        asksLock = new ReentrantLock();

        // Primary ISIN indexing for bids and asks
        bids = new ConcurrentHashMap<>();
        asks = new ConcurrentHashMap<>();

        orders = new ConcurrentHashMap<>();
    }

    /**
     * Instance control with Init-on-demand Holder class.
     */
    private static class InstanceHolder {
        static final Map<String, OrderBook> INSTANCES;
        static final List<String> SUPPORTED_MARKETS;

        static {
            INSTANCES = new ConcurrentHashMap<>();
            SUPPORTED_MARKETS = List.of("NYSE", "NASDAQ", "TSX", "BMV", "LSE", "Euronext", "FWB", "TSE", "KRX");
            SUPPORTED_MARKETS.forEach(market -> INSTANCES.put(market, new OrderBook(market)));
        }
    }

    public static OrderBook forMarket(String market) {
        OrderBook orderBook = InstanceHolder.INSTANCES.get(market);
        if (orderBook == null) {
            throw new IllegalArgumentException(String.format("Unsupported market: %s", market));
        }
        return orderBook;
    }

    @Override
    public void addOrder(Order order) {
        validateOrder(order);

        var priceLevels = getPriceLevels(order.getSide());
        var lock = getLock(order.getSide());

        lock.lock();
        try {
            if (orders.containsKey(order.getOrderId())) {
                log.info("Order with ID: {} already exists.", order.getOrderId());
                return;
            }
            // Get or create the ISIN index
            var isinIndex = priceLevels.computeIfAbsent(order.isin(), k -> initInnerMap(order.getSide()));
            // Get or create the price level list
            var ordersAtPrice = isinIndex.computeIfAbsent(order.getPrice(), k -> new ArrayList<>());
            ordersAtPrice.add(order);

            orders.put(order.getOrderId(), order);
            log.info("Added Order: {} to OrderBook.\nOrder count={} for market={}", order, orders.size(), market);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Order> removeOrder(Integer id) {
        Order order = orders.remove(id);
        if (order == null) {
            throw new NoSuchElementException(String.format("Order with ID: %s not found.", id));
        }

        var priceLevels = getPriceLevels(order.getSide());
        var lock = getLock(order.getSide());

        lock.lock();
        try {
            var isinIndex = priceLevels.get(order.isin());
            if (isinIndex == null) {
                throw new NoSuchElementException(String.format("No orders found for ISIN: %s", order.isin()));
            }

            var ordersAtPrice = isinIndex.get(order.getPrice());
            if (ordersAtPrice == null || ordersAtPrice.isEmpty()) {
                throw new NoSuchElementException(String.format("No orders at price: %s", order.getPrice()));
            }

            ordersAtPrice.remove(order);
            if (ordersAtPrice.isEmpty()) {
                isinIndex.remove(order.getPrice());
            }
            if (isinIndex.isEmpty()) {
                priceLevels.remove(order.isin());
            }
        } finally {
            lock.unlock();
        }

        return Optional.of(order);
    }

    @Override
    public List<Order> getMatchingOrders(BigDecimal price, String isin, Side side, String orderType) {
        var priceLevels = getMatchingPriceLevels(side);
        var lock = getLock(side);

        lock.lock();
        try {
            // Get the ISIN-specific price levels
            var isinIndex = priceLevels.get(isin);
            if (isinIndex == null) {
                return List.of(); // No orders for the ISIN
            }

            // Get the price range
            SortedMap<BigDecimal, List<Order>> priceRange = switch (orderType) {
                case MARKET_ORDER -> (side == BUY) ? isinIndex.tailMap(price, true) : isinIndex.headMap(price, true);
                case LIMIT_ORDER -> (side == BUY) ? isinIndex.headMap(price, true) : isinIndex.tailMap(price, true);
                default -> {
                    throw new IllegalArgumentException(String.format("Unsupported order type: %s", orderType));
                }
            };

            // Flatten the orders
            return priceRange.values().stream().flatMap(List::stream).toList();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Order> getOrder(Integer id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public String getMarket() {
        return market;
    }

    @Override
    public int size() {
        return orders.size();
    }

    @Override
    public void clear() {
        bids.clear();
        asks.clear();
        orders.clear();
        log.info("Cleared OrderBook for Market: {}", market);
    }

    private TreeMap<BigDecimal, List<Order>> initInnerMap(Side side) {
        return (side == BUY) ? new TreeMap<>(Comparator.reverseOrder()) : new TreeMap<>(Comparator.naturalOrder());
    }
    private Map<String, TreeMap<BigDecimal, List<Order>>> getPriceLevels(Side side) {
        return (side == BUY) ? bids : asks;
    }

    private Map<String, TreeMap<BigDecimal, List<Order>>> getMatchingPriceLevels(Side side) {
        return (side == BUY) ? asks : bids;
    }

    private ReentrantLock getLock(Side side) {
        return (side == BUY) ? bidsLock : asksLock;
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        if (order.getOrderId() == null || order.getOrderId() <= 0) {
            throw new IllegalArgumentException("Invalid order ID: " + order.getOrderId());
        }
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid order price: " + order.getPrice());
        }
        if (order.getQuantity() <= 0 && order.getRemainingQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid order quantity: " + order.getQuantity());
        }
        if (!order.market().equals(market)) {
            throw new IllegalArgumentException(
                    String.format("Invalid market code for order: %s. Expected: %s.", order.market(), market));
        }
    }
}
