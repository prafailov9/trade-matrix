package com.ntros.processor.order;


import com.ntros.dto.order.request.OrderRequest;
import com.ntros.dto.order.response.OrderResponse;

import java.util.concurrent.CompletableFuture;

public interface OrderProcessor<S extends OrderRequest, R extends OrderResponse> {

    R processOrder(S orderRequest);

}
