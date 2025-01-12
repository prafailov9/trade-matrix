package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.model.order.Order;

public interface CreateOrderInitializer {

    Order initialize(CreateOrderRequest createOrderRequest);

}
