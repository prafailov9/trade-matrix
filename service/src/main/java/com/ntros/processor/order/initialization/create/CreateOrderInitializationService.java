package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
     * Creates an initializer based on the order's transaction type.
     * Matches the transactionType name to the initializer service's name(BUY/SELL)
     *
     * @param createOrderRequest - order to initialize
     * @return - initialized order object
     */
    @Override
    @Transactional
    public Order initializeOrder(CreateOrderRequest createOrderRequest) {
        CreateOrderInitializer initializer = createOrderInitializers.get(createOrderRequest.getTransactionType().toLowerCase());
        if (initializer == null) {
            throw new IllegalArgumentException("Unknown order type: " + createOrderRequest.getTransactionType());
        }
        return initializer.initialize(createOrderRequest);
    }


}
