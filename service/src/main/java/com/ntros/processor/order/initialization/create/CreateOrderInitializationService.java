package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * First step of the order processing.
 * Initialization is based on the order's directive(BUY/SELL).
 */
@Service
@Slf4j
public class CreateOrderInitializationService implements CreateOrderInitialization {


    private final Map<String, CreateOrderInitializer> createOrderInitializers;


    @Autowired
    public CreateOrderInitializationService(Map<String, CreateOrderInitializer> createOrderInitializers) {
        this.createOrderInitializers = createOrderInitializers;
    }


    /**
     * Instantiates an initializer service object based on the order's transaction type(directive).
     * Matches the transactionType name to the initializer service's name(BUY/SELL)
     * @param createOrderRequest - order to initialize
     * @return - initialized order domain object
     */
    @Override
    public CompletableFuture<Order> initializeOrder(CreateOrderRequest createOrderRequest) {
        CreateOrderInitializer initializer = createOrderInitializers.get(createOrderRequest.getTransactionType().toLowerCase());
        if (initializer == null) {
            throw new IllegalArgumentException("Unknown order type: " + createOrderRequest.getTransactionType());
        }
        return initializer.initialize(createOrderRequest);
    }


}
