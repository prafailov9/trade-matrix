package com.ntros.service.order;

import com.ntros.cache.OrderBook;
import com.ntros.exception.DataConstraintFailureException;
import com.ntros.exception.NotFoundException;
import com.ntros.model.order.*;
import com.ntros.model.product.MarketProduct;
import com.ntros.order.OrderRepository;
import com.ntros.order.OrderStatusRepository;
import com.ntros.order.OrderTypeRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.ntros.model.order.Side.BUY;
import static com.ntros.model.order.Side.SELL;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class OrderDataService implements OrderService {

    private final Executor executor;
    private final OrderRepository orderRepository;
    private final OrderTypeRepository orderTypeRepository;
    private final OrderStatusRepository orderStatusRepository;


    @Autowired
    public OrderDataService(@Qualifier("taskExecutor") Executor executor,
                            OrderRepository orderRepository,
                            OrderTypeRepository orderTypeRepository,
                            OrderStatusRepository orderStatusRepository) {

        this.executor = executor;
        this.orderRepository = orderRepository;
        this.orderTypeRepository = orderTypeRepository;
        this.orderStatusRepository = orderStatusRepository;
    }

    @Override
    public Order createOrder(Order order) {
        try {
            Order saved = orderRepository.save(order);

            // add to order book
            OrderBook.forMarket(order.market()).addOrder(order);
            log.info("Saved order: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | OptimisticLockException ex) {
            log.error("Could not save order {}. {}", order, ex.getMessage(), ex);
            throw handleOptimisticLockAndThrow(ex); // rethrowing lock exception to handle retries
        }

    }

    @Override
    public Order getOrder(String accountNumber, String productIsin) {
        return orderRepository.findByAccountNumberProductIsinOrderStatus(accountNumber, productIsin)
                .orElseThrow(() ->
                        NotFoundException.with(
                                format("Could not find order for AN=%s, isin=%s", accountNumber, productIsin)));
    }

    @Override
    public Order updateOrder(Integer orderId, Order order) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        NotFoundException.with(
                                format("Order not found for id: %s", orderId)));

        // remove existing order from OrderBOok
        OrderBook.forMarket(order.market()).removeOrder(orderId);

        // update all fields
        existingOrder.setOrderType(order.getOrderType());
        existingOrder.setWallet(order.getWallet());
        existingOrder.setPrice(order.getPrice());
        existingOrder.setMarketProduct(order.getMarketProduct());
        existingOrder.setQuantity(order.getQuantity());
        existingOrder.setFilledQuantity(order.getFilledQuantity());
        existingOrder.setRemainingQuantity(order.getRemainingQuantity());
        existingOrder.setPlacedAt(order.getPlacedAt());

        // Save updated order
        Order updatedOrder = orderRepository.save(existingOrder);

        // Add the updated order to the OrderBook
        OrderBook.forMarket(order.market()).addOrder(updatedOrder);

        return updatedOrder;
    }

    @Override
    public CompletableFuture<List<Order>> getAllOrders() {
        return supplyAsync(() -> Optional.of(orderRepository.findAll())
                .orElseThrow(() -> NotFoundException.with("No orders found.")));
    }

    @Override
    public CompletableFuture<List<Order>> getAllOpenOrders() {
        return supplyAsync(() -> Optional.of(orderRepository.findAllOpen())
                .orElseThrow(() -> NotFoundException.with("No OPEN orders found.")));
    }

    @Override
    public CompletableFuture<List<Order>> getAllFilledOrders() {
        return supplyAsync(() -> Optional.of(orderRepository.findAllFilled())
                .orElseThrow(() -> NotFoundException.with("No OPEN orders found.")));
    }

    @Override
    public CompletableFuture<List<Order>> getAllPartialOrders() {
        return supplyAsync(() -> Optional.of(orderRepository.findAllPartial())
                .orElseThrow(() -> NotFoundException.with("No OPEN orders found.")));

    }

    /**
     * Finds all matching OPEN orders based on it's Side, available products and price
     */
    @Override
    public List<Order> findMatchingOrders(Order order) {
        return order.getSide().equals(Side.BUY)
                ? findMatchingSellOrders(order.getMarketProduct(), order.getPrice(), order.getOrderType().getOrderTypeName())
                : findMatchingBuyOrders(order.getMarketProduct(), order.getPrice(), order.getOrderType().getOrderTypeName());
    }


    public List<Order> findMatchingSellOrders(MarketProduct marketProduct, BigDecimal bidPrice, String orderType) {
        List<Order> orders = OrderBook.forMarket(marketProduct.getMarket().getMarketCode())
                .getMatchingOrders(bidPrice, marketProduct.getProduct().getIsin(), BUY, orderType);

        return (orders != null && !orders.isEmpty())
                ? orders
                : orderRepository.findAllMatchingAsks(marketProduct, bidPrice);
    }

    public List<Order> findMatchingBuyOrders(MarketProduct marketProduct, BigDecimal askPrice, String orderType) {
        List<Order> orders = OrderBook.forMarket(marketProduct.getMarket().getMarketCode())
                .getMatchingOrders(askPrice, marketProduct.getProduct().getIsin(), SELL, orderType);

        return (orders != null && !orders.isEmpty())
                ? orders
                : orderRepository.findAllMatchingBids(marketProduct, askPrice);
    }

    @Override
    public OrderType getOrderType(String type) {
        return orderTypeRepository.findOneByOrderTypeName(type)
                .orElseThrow(() -> NotFoundException.with(format("Order type not found for: %s", type)));
    }

    @Override
    public CompletableFuture<List<OrderStatus>> getAllByOrder(Order order) {
        return supplyAsync(() -> getOrderStatus(order), executor);
    }

    @Override
    public OrderStatus updateOrderStatus(Order order, CurrentOrderStatus orderStatus) {
        try {
            log.info("Updating status:{} for order:{}", orderStatus, order);
            return orderStatusRepository.save(OrderStatus.builder()
                    .order(order)
                    .currentStatus(orderStatus.name())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.info("Failed to update status:{} for order:{}", orderStatus, order);
            throw DataConstraintFailureException.with(
                    format(
                            "Could not update order status with given values: order: %s, status: %s",
                            order,
                            orderStatus.name()),
                    ex);
        }
    }

    @Override
    public OrderStatus determineAndUpdateCurrentStatus(Order order) {
        if (order.getQuantity() == order.getFilledQuantity() && order.getRemainingQuantity() == 0) {
            OrderStatus status = updateOrderStatus(order, CurrentOrderStatus.FILLED);
            // remove from order book
            OrderBook.forMarket(order.market()).removeOrder(order.getOrderId());
            return status;
        }
        return updateOrderStatus(order, CurrentOrderStatus.PARTIALLY_FILLED);
    }

    @Override
    public void transferFunds(Order buyOrder, Order sellOrder, int matchedQuantity) {
        log.info("Transferring funds for matched orders: Buy={}, Sell={}, Quantity={}",
                buyOrder, sellOrder, matchedQuantity);

        buyOrder.adjustQuantity(matchedQuantity);
        determineAndUpdateCurrentStatus(buyOrder);

        sellOrder.adjustQuantity(matchedQuantity);
        determineAndUpdateCurrentStatus(sellOrder);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);
    }

    private List<OrderStatus> getOrderStatus(Order order) {
        return orderStatusRepository.findAllByOrder(order);
    }

    private OrderStatus getOrderStatus(Order order, CurrentOrderStatus currentOrderStatus) {
        return orderStatusRepository.findOneByOrderCurrentStatus(order, currentOrderStatus)
                .orElseThrow(() ->
                        NotFoundException.with(
                                format("status not found for order: %s", order)));
    }

    private RuntimeException handleOptimisticLockAndThrow(RuntimeException ex) {
        return ex instanceof OptimisticLockException || ex instanceof ObjectOptimisticLockingFailureException
                ? ex
                : DataConstraintFailureException.with(ex.getMessage(), ex);
    }

}
