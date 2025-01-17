package com.ntros.cache;

import com.ntros.model.order.Order;
import com.ntros.model.order.Side;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.ntros.cache.LockingUtil.runSafe;
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

    // cache locks
    private final ReentrantLock bidsLock;
    private final ReentrantLock asksLock;

    private OrderBook(String market) {
        this.market = market;

        bidsLock = new ReentrantLock();
        asksLock = new ReentrantLock();

        bids = new ConcurrentHashMap<>();
        asks = new ConcurrentHashMap<>();
        orders = new ConcurrentHashMap<>();
    }

    /**
     * Instance control with Init-on-demand Holder class: <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">...</a>.
     * Creates multiple predefined instances of an OrderBook per market.
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

    /**
     * Gets an OrderBook for a specific market.
     * The very 1st call will initialize the InstanceHolder class and the OrderBook instances.
     *
     * @param market - to retrieve an OrderBook
     * @return - OrderBook
     */
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
        var isinIndex = getIsinIndex(order.getSide());

        if (orders.containsKey(order.getOrderId())) {
            log.info("Order with ID: {} already exists.", order.getOrderId());
            return;
        }

        // prices creates a new [price:map] entry if none exists for the price key
        var priceIndex = isinIndex.computeIfAbsent(order.isin(), k -> initializeInnerMap(order.getSide()));

        // get or create the price level list
        runSafe(getLock(order.getSide()),
                () -> {
                    var ordersAtPrice = priceIndex.computeIfAbsent(order.getPrice(), k -> new ArrayList<>());
                    ordersAtPrice.add(order);
                });

        orders.put(order.getOrderId(), order);
        log.info("Added Order: {} to OrderBook.\nOrder count={} for market={}", order, orders.size(), market);
    }

    @Override
    public Optional<Order> removeOrder(Integer id) {
        Order order = orders.remove(id);
        if (order == null) {
            throw new NoSuchElementException(String.format("Order with ID: %s not found.", id));
        }

        var isinIndex = getIsinIndex(order.getSide());

        isinIndex.computeIfPresent(order.isin(), (isin, priceIndex) ->
                runSafe(getLock(order.getSide()), () -> {
                    priceIndex.computeIfPresent(order.getPrice(), (price, ordersAtPrice) -> {
                        ordersAtPrice.remove(order);
                        // ff the list becomes empty, remove the price index
                        return ordersAtPrice.isEmpty() ? null : ordersAtPrice;
                    });
                    // if the price index becomes empty, remove the ISIN
                    return priceIndex.isEmpty() ? null : priceIndex;
                }));

        return Optional.of(order);
    }


    /**
     * map.keys = [1, 2, 3, 4, 5, 6, 7], x = 5
     * map.tail(x, true) = [5, 6, 7]
     * map.head(x, true) = [1, 2, 3, 4, 5]
     */
    @Override
    public List<Order> getMatchingOrders(BigDecimal price, String isin, Side side, String orderType) {
        var isinIndex = getMatchingIsinIndex(side);

        return runSafe(getLock(side), () -> {
            var priceIndex = isinIndex.get(isin);
            if (priceIndex == null) {
                return List.of(); // no orders for the ISIN
            }

            // get the price range by order type
            SortedMap<BigDecimal, List<Order>> priceRange =
                    switch (orderType) {
                        case MARKET_ORDER ->
                                (side == BUY) ? priceIndex.tailMap(price, true) : priceIndex.headMap(price, true);
                        case LIMIT_ORDER ->
                                (side == BUY) ? priceIndex.headMap(price, true) : priceIndex.tailMap(price, true);
                        default ->
                                throw new IllegalArgumentException(String.format("Unsupported order type: %s", orderType));
                    };

            return priceRange.values().stream().flatMap(List::stream).toList();
        });
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

    private TreeMap<BigDecimal, List<Order>> initializeInnerMap(Side side) {
        return (side == BUY) ? new TreeMap<>(Comparator.reverseOrder()) : new TreeMap<>(Comparator.naturalOrder());
    }

    private Map<String, TreeMap<BigDecimal, List<Order>>> getIsinIndex(Side side) {
        return (side == BUY) ? bids : asks;
    }

    private Map<String, TreeMap<BigDecimal, List<Order>>> getMatchingIsinIndex(Side side) {
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
