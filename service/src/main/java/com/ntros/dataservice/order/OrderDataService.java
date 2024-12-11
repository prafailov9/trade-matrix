package com.ntros.dataservice.order;

import com.ntros.exception.*;
import com.ntros.model.order.*;
import com.ntros.model.product.Product;
import com.ntros.order.OrderRepository;
import com.ntros.order.OrderStatusRepository;
import com.ntros.order.OrderTypeRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
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
    public CompletableFuture<Order> findOrderById(Integer orderId) {
        return supplyAsync(() -> orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
    }

    @Transactional
    @Modifying
    public CompletableFuture<Order> createOrder(Order order) {
        return supplyAsync(() -> {
            try {
                return orderRepository.save(order);
            } catch (DataIntegrityViolationException | OptimisticLockException ex) {
                log.error("Could not save order {}. {}", order, ex.getMessage(), ex);
                throw handleOptimisticLockAndThrow(ex); // rethrowing lock exception to handle retries
            }
        }, executor);

    }

    @Override
    public CompletableFuture<Order> getOrder(String accountNumber, String productIsin) {
        return supplyAsync(() -> orderRepository
                                .findByAccountNumberProductIsinOrderStatus(accountNumber, productIsin)
                                .orElseThrow(() -> new OrderNotFoundException(
                                        String.format("Could not find order for AN=%s, isin=%s",
                                                accountNumber,
                                                productIsin))),
                        executor);

    }

    @Override
    public CompletableFuture<Void> updateOrder(Order order) {
        return CompletableFuture.runAsync(() -> {
            Order savedOrder = orderRepository.findById(order.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(
                            String.format("Order not found %s", order)));

            // update all fields
            savedOrder.setOrderType(order.getOrderType());
            savedOrder.setWallet(order.getWallet());
            savedOrder.setPrice(order.getPrice());
            savedOrder.setProduct(order.getProduct());
            savedOrder.setQuantity(order.getQuantity());
            savedOrder.setFilledQuantity(order.getFilledQuantity());
            savedOrder.setRemainingQuantity(order.getRemainingQuantity());
            savedOrder.setPlacedAt(order.getPlacedAt());

            orderRepository.save(savedOrder);
        });
    }

    @Override
    public CompletableFuture<List<Order>> getAllOrders() {
        return supplyAsync(() ->
                Optional.of(orderRepository.findAll())
                        .orElseThrow(() -> new OrderNotFoundException("Could Not find any orders.")));
    }

    /**
     * Finds all matching OPEN orders based on it's Side, available products and price
     * @param order
     * @return
     */
    @Override
    public CompletableFuture<List<Order>> findMatchingOrders(Order order) {
        return order.getSide().equals(Side.BUY)
                ? findMatchingSellOrders(order.getProduct(), order.getPrice())
                : findMatchingBuyOrders(order.getProduct(), order.getPrice());
    }

    @Override
    public CompletableFuture<List<Order>> findMatchingBuyOrders(Product product, BigDecimal price) {
        return supplyAsync(() -> orderRepository.findAllByMatchingBuyOrders(product, price), executor);
    }


    @Override
    public CompletableFuture<List<Order>> findMatchingSellOrders(Product product, BigDecimal price) {
        return supplyAsync(() -> orderRepository.findAllByMatchingSellOrders(product, price), executor);
    }

    @Override
    public CompletableFuture<OrderType> getOrderType(String type) {
        return supplyAsync(() ->
                orderTypeRepository.findOneByOrderTypeName(type)
                        .orElseThrow(() ->
                                new OrderTypeNotFoundException(
                                        String.format("Order type not found for: %s", type))), executor);
    }

    @Override
    public CompletableFuture<String> deleteAllOrders() {
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
    public CompletableFuture<OrderStatus> updateOrderStatus(Order order, CurrentOrderStatus orderStatus) {
        return supplyAsync(() -> {
            try {
                return orderStatusRepository.save(OrderStatus.builder()
                        .order(order)
                        .currentStatus(orderStatus.name())
                        .build());
            } catch (DataIntegrityViolationException ex) {
                throw new OrderStatusCreateFailedException(
                        String.format("Could not update order status with given values: order - %s, status - %s",
                                order, orderStatus.name()),ex);
            }
        }, executor);
    }


    @Override
    public CompletableFuture<OrderStatus> determineAndUpdateCurrentStatus(Order order) {
        return order.getQuantity() == 0
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
        return ex instanceof OptimisticLockException
                ? ex
                : new OrderConstraintViolationException(ex.getMessage(), ex);
    }

}
