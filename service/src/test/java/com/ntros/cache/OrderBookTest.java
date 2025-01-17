package com.ntros.cache;

import com.ntros.model.currency.Currency;
import com.ntros.model.market.Market;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import com.ntros.model.order.OrderType;
import com.ntros.model.order.Side;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static com.ntros.cache.OrderBook.forMarket;
import static com.ntros.model.order.Side.BUY;
import static com.ntros.model.order.Side.SELL;
import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    private static final String TEST_MARKET = "NASDAQ";
    private static final String INVALID_MARKET = "113dwd212d2";
    private static final String MARKET_ORDER_TYPE = "MARKET";
    private static final String LIMIT_ORDER_TYPE = "LIMIT";

    private OrderBook orderBook;

    private final OrderType orderType = new OrderType(1, MARKET_ORDER_TYPE);
    private Order buyOrder1;
    private Order buyOrder2;
    private Order sellOrder1;
    private Order sellOrder2;

    Product prod = new Product();
    Currency currency = new Currency();
    Market market = new Market();
    MarketProduct marketProduct = new MarketProduct();
    OrderStatus orderStatus = new OrderStatus();


    @BeforeEach
    void setUp() {
        orderBook = forMarket(TEST_MARKET);
        // clearing the instance state before each test
        orderBook.clear();

        prod.setProductName("Apple Inc.");
        prod.setIsin("US0378331005");

        currency.setCurrencyCode("USD");

        market.setMarketCode(TEST_MARKET);
        market.setCurrency(currency);

        marketProduct.setProduct(prod);
        marketProduct.setMarket(market);

        orderStatus.setCurrentStatus("OPEN");

        buyOrder1 = createOrder(1, BUY, BigDecimal.valueOf(100), 10, marketProduct);
        buyOrder2 = createOrder(2, BUY, BigDecimal.valueOf(105), 5, marketProduct);
        sellOrder1 = createOrder(3, SELL, BigDecimal.valueOf(110), 8, marketProduct);
        sellOrder2 = createOrder(4, SELL, BigDecimal.valueOf(120), 12, marketProduct);
    }

    @Test
    void getOrderBookInstance_forMarket_successfulInitialization() {
        OrderBook orderBook = forMarket(TEST_MARKET);
        assertNotNull(orderBook);
        assertEquals(TEST_MARKET, orderBook.getMarket());
    }

    @Test
    void getOrderBookInstance_forMarket_failsToInitialize() {
        String expectedError = "Unsupported market: " + INVALID_MARKET;
        try {
            forMarket(INVALID_MARKET);
        } catch (IllegalArgumentException ex) {
            assertEquals(expectedError, ex.getMessage());
        }
    }

    @Test
    void getMatchingBuysForSellOrder() {
        orderBook.addOrder(buyOrder1); // Price: 100
        orderBook.addOrder(buyOrder2); // Price: 105


        List<Order> matchingOrders = orderBook.getMatchingOrders(BigDecimal.valueOf(101), prod.getIsin(), SELL, MARKET_ORDER_TYPE);

        assertEquals(1, matchingOrders.size(), "Should return buy orders with price <= 102.");
        assertEquals(BigDecimal.valueOf(105), matchingOrders.get(0).getPrice(), "Matching order should have the highest buy price.");
    }

    @Test
    void getMatchingSellsForBuyOrder() {
        orderBook.addOrder(sellOrder1); // Price: 110
        orderBook.addOrder(sellOrder2); // Price: 120


        List<Order> matchingOrders = orderBook.getMatchingOrders(BigDecimal.valueOf(115), prod.getIsin(), BUY, MARKET_ORDER_TYPE);

        assertEquals(1, matchingOrders.size(), "Should return sell orders with price >= 115.");
        assertEquals(BigDecimal.valueOf(120), matchingOrders.get(0).getPrice(), "Matching order should have the lowest sell price.");
    }

    @Test
    void addOrder_skipAddDuplicateIDs() {
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder1);

        assertEquals(1, orderBook.size(), "Should not add an order which already exists.");
    }

    @Test
    void removeOrder_emptyOrderBook_skipOperation() {
        assertThrows(NoSuchElementException.class, () -> orderBook.removeOrder(999), "Removing a nonexistent order should throw an exception.");
    }

    @Test
    void getMatchingOrders_whenNoneFound_returnEmptyList() {
        orderBook.addOrder(sellOrder1);

        List<Order> matchingOrders = orderBook.getMatchingOrders(BigDecimal.valueOf(90), "123", BUY, MARKET_ORDER_TYPE);

        assertTrue(matchingOrders.isEmpty(), "Should return an empty list when no matching orders are found.");
    }

    @Test
    void getAllMatching_filterByPriceAndIsin() {
        Order expectedMatching = createOrder(5, BUY, BigDecimal.valueOf(100), 10, marketProduct);
        orderBook.addOrder(expectedMatching);

        // new product
        Product product = new Product();
        product.setIsin("ISIN123");
        MarketProduct mp = new MarketProduct();
        mp.setMarket(marketProduct.getMarket());
        mp.setProduct(product);

        orderBook.addOrder(createOrder(6, BUY, BigDecimal.valueOf(105), 10, mp));

        List<Order> matchingOrders = orderBook.getMatchingOrders(BigDecimal.valueOf(90), prod.getIsin(), SELL, MARKET_ORDER_TYPE);

        assertEquals(1, matchingOrders.size(), "Should return only orders matching the price and ISIN.");
        assertEquals(expectedMatching, matchingOrders.get(0));
        assertEquals(prod.getIsin(), matchingOrders.get(0).getMarketProduct().getProduct().getIsin(), "Returned order must have the correct ISIN.");
    }

    private Order createOrder(Integer id, Side side, BigDecimal price, int qty, MarketProduct mp) {
        return Order.builder()
                .orderId(id)
                .side(side)
                .price(price)
                .quantity(qty)
                .remainingQuantity(qty)
                .filledQuantity(0)
                .marketProduct(mp)
                .orderType(orderType)
                .orderStatuses(List.of(orderStatus))
                .build();
    }

}