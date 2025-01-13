package com.ntros.config;

import com.ntros.cache.OrderBookCache;
import com.ntros.model.order.Order;
import com.ntros.order.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupCacheInitializer implements CommandLineRunner {


    private final OrderRepository orderRepository;
    public StartupCacheInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Order> orders = orderRepository.findAllOpenOrders();
        orders.forEach(order -> {
            String marketCode = order.getMarketProduct().getMarket().getMarketCode();
            OrderBookCache cache = OrderBookCache.getInstance(marketCode);
            cache.put(order.getOrderId(), order);
        });
    }
}
