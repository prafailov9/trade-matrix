package com.ntros.service.order;

import com.ntros.exception.*;
import com.ntros.model.order.*;
import com.ntros.model.product.MarketProduct;
import com.ntros.order.OrderRepository;
import com.ntros.order.OrderStatusRepository;
import com.ntros.order.OrderTypeRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class OrderDataService implements OrderService {

    private final Executor executor;
    private final OrderRepository orderRepository;
    private final OrderTypeRepository orderTypeRepository;
    private final OrderStatusRepository orderStatusRepository;


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
    public CompletableFuture<Order> findOrderByIdAsync(Integer orderId) {
        return supplyAsync(() -> orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
    }

    @Transactional
    @Modifying
    public CompletableFuture<Order> createOrderAsync(Order order) {
        return supplyAsync(() -> createOrder(order), executor);

    }

    @Override
    public Order createOrder(Order order) {
        try {
            Order saved = orderRepository.save(order);
            log.info("[IN OrderService.createOrder()]\nSaved order: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | OptimisticLockException ex) {
            log.error("[IN OrderService.createOrder()]\nCould not save order {}. {}", order, ex.getMessage(), ex);
            throw handleOptimisticLockAndThrow(ex); // rethrowing lock exception to handle retries
        }

    }

    @Override
    public CompletableFuture<Order> getOrderAsync(String accountNumber, String productIsin) {
        return supplyAsync(() -> orderRepository
                        .findByAccountNumberProductIsinOrderStatus(accountNumber, productIsin)
                        .orElseThrow(() -> new OrderNotFoundException(
                                String.format("Could not find order for AN=%s, isin=%s",
                                        accountNumber,
                                        productIsin))),
                executor);

    }

    @Override
    public CompletableFuture<Void> updateOrderAsync(Order order) {
        return CompletableFuture.runAsync(() -> {
            Order savedOrder = orderRepository.findById(order.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(
                            String.format("Order not found %s", order)));

            // update all fields
            savedOrder.setOrderType(order.getOrderType());
            savedOrder.setWallet(order.getWallet());
            savedOrder.setPrice(order.getPrice());
            savedOrder.setMarketProduct(order.getMarketProduct());
            savedOrder.setQuantity(order.getQuantity());
            savedOrder.setFilledQuantity(order.getFilledQuantity());
            savedOrder.setRemainingQuantity(order.getRemainingQuantity());
            savedOrder.setPlacedAt(order.getPlacedAt());

            orderRepository.save(savedOrder);
        });
    }

    @Override
    public CompletableFuture<List<Order>> getAllOrdersAsync() {
        return supplyAsync(() ->
                Optional.of(orderRepository.findAll())
                        .orElseThrow(() -> new OrderNotFoundException("Could Not find any orders.")));
    }

    /**
     * Finds all matching OPEN orders based on it's Side, available products and price
     *
     * @param order
     * @return
     */
    @Override
    public CompletableFuture<List<Order>> findMatchingOrdersAsync(Order order) {
        return order.getSide().equals(Side.BUY)
                ? findMatchingSellOrdersAsync(order.getMarketProduct(), order.getPrice())
                : findMatchingBuyOrdersAsync(order.getMarketProduct(), order.getPrice());
    }

    @Override
    public List<Order> findMatchingOrders(Order order) {
        return order.getSide().equals(Side.BUY)
                ? findMatchingSellOrders(order.getMarketProduct(), order.getPrice())
                : findMatchingBuyOrders(order.getMarketProduct(), order.getPrice());
    }

    @Override
    @Transactional
    public CompletableFuture<List<Order>> findMatchingBuyOrdersAsync(MarketProduct marketProduct, BigDecimal price) {
        return supplyAsync(() -> findMatchingBuyOrders(marketProduct, price), executor);
    }

    @Override
    public List<Order> findMatchingBuyOrders(MarketProduct marketProduct, BigDecimal price) {
        return orderRepository.findAllByMatchingBuyOrders(marketProduct, price);
    }


    @Override
    @Transactional
    public CompletableFuture<List<Order>> findMatchingSellOrdersAsync(MarketProduct marketProduct, BigDecimal price) {
        return supplyAsync(() -> findMatchingSellOrders(marketProduct, price), executor);
    }

    @Override
    public List<Order> findMatchingSellOrders(MarketProduct marketProduct, BigDecimal price) {
        return orderRepository.findAllByMatchingSellOrders(marketProduct, price);
    }

    @Override
    public CompletableFuture<OrderType> getOrderTypeAsync(String type) {
        return supplyAsync(() ->
                getOrderType(type), executor);
    }

    @Override
    public OrderType getOrderType(String type) {
        return orderTypeRepository.findOneByOrderTypeName(type)
                .orElseThrow(() ->
                        new OrderTypeNotFoundException(
                                String.format("Order type not found for: %s", type)));
    }

    @Override
    public CompletableFuture<String> deleteAllOrdersAsync() {
        return supplyAsync(() -> {
            String res;
            try {
                orderRepository.deleteAll();
                res = "success";
            } catch (DataIntegrityViolationException ex) {
                log.error("Error occurred while deleting order table: {}", ex.getMessage(), ex);
                throw new FailedOrdersDeleteException("Failed to delete all existing orders");
            }
            return res;
        }, executor);
    }

    @Override
    public CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order) {
        return supplyAsync(() -> getOrderStatus(order), executor);
    }

    @Override
    public CompletableFuture<OrderStatus> getOrderStatusByOrder(Order order, CurrentOrderStatus status) {
        return supplyAsync(() -> getOrderStatus(order, status), executor);
    }


    @Override
    public CompletableFuture<OrderStatus> updateOrderStatusAsync(Order order, CurrentOrderStatus orderStatus) {
        return supplyAsync(() -> updateOrderStatus(order, orderStatus), executor);
    }

    @Override
    public OrderStatus updateOrderStatus(Order order, CurrentOrderStatus orderStatus) {
        try {
            log.info("[IN OrderDataService.updateOrderStatus()]\nUpdating status:{} for order:{}", orderStatus, order);
            return orderStatusRepository.save(OrderStatus.builder()
                    .order(order)
                    .currentStatus(orderStatus.name())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.info("[IN OrderDataService.updateOrderStatus()]\n Failed to update status:{} for order:{}", orderStatus, order);
            throw new OrderStatusCreateFailedException(
                    String.format("Could not update order status with given values: order: %s, status: %s",
                            order, orderStatus.name()), ex);
        }
    }


    @Override
    public CompletableFuture<OrderStatus> determineAndUpdateCurrentStatusAsync(Order order) {
        return order.getQuantity() == order.getFilledQuantity() && order.getRemainingQuantity() == 0
                ? updateOrderStatusAsync(order, CurrentOrderStatus.FILLED)
                : updateOrderStatusAsync(order, CurrentOrderStatus.PARTIALLY_FILLED);
    }

    @Override
    public OrderStatus determineAndUpdateCurrentStatus(Order order) {
        return order.getQuantity() == order.getFilledQuantity() && order.getRemainingQuantity() == 0
                ? updateOrderStatus(order, CurrentOrderStatus.FILLED)
                : updateOrderStatus(order, CurrentOrderStatus.PARTIALLY_FILLED);
    }

    private OrderStatus getOrderStatus(Order order) {
        return orderStatusRepository.findOneByOrder(order)
                .orElseThrow(() -> new OrderStatusNotFoundException(
                        String.format("status not found for order: %s", order)));
    }

    private OrderStatus getOrderStatus(Order order, CurrentOrderStatus currentOrderStatus) {
        return orderStatusRepository.findOneByOrderCurrentStatus(order, currentOrderStatus)
                .orElseThrow(() -> new OrderStatusNotFoundException(
                        String.format("status not found for order: %s", order)));
    }

    private RuntimeException handleOptimisticLockAndThrow(RuntimeException ex) {
        return ex instanceof OptimisticLockException || ex instanceof ObjectOptimisticLockingFailureException
                ? ex
                : new OrderConstraintViolationException(ex.getMessage(), ex);
    }

}
