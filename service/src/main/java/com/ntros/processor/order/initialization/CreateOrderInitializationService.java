package com.ntros.processor.order.initialization;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CreateOrderInitializationService implements CreateOrderInitialization {


    private final Map<String, CreateOrderInitializer> createOrderInitializers;


    @Autowired
    public CreateOrderInitializationService(Map<String, CreateOrderInitializer> createOrderInitializers) {
        this.createOrderInitializers = createOrderInitializers;
    }


    @Override
    public CompletableFuture<Order> initializeOrder(CreateOrderRequest createOrderRequest) {
        CreateOrderInitializer initializer = createOrderInitializers.get(createOrderRequest.getTransactionType().toLowerCase());
        if (initializer == null) {
            throw new IllegalArgumentException("Unknown order type: " + createOrderRequest.getTransactionType());
        }
        return initializer.initialize(createOrderRequest);
    }


}
