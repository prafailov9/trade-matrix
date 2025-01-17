package com.ntros.config;

import com.ntros.cache.OrderBook;
import com.ntros.model.order.Order;
import com.ntros.order.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StartupCacheInitializer implements CommandLineRunner {


    private final OrderRepository orderRepository;

    public StartupCacheInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        List<Order> allOpen = orderRepository.findAllOpen();
        List<Order> allPartial = orderRepository.findAllPartial();
        allOpen.addAll(allPartial);
        allOpen.forEach(order -> OrderBook.forMarket(order.market()).addOrder(order));
      log.info("Initialized Order Book");
    }
}
