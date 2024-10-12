package com.ntros.controller.order;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.dto.order.response.CreateOrderResponse;
import com.ntros.processor.order.OrderProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("api/order-process")
public class CreateOrderProcessorController extends AbstractOrderProcessorController<CreateOrderRequest, CreateOrderResponse> {


    public CreateOrderProcessorController(Executor executor,
                                          OrderProcessor<CreateOrderRequest, CreateOrderResponse> orderProcessor) {

        super(executor, orderProcessor);
    }

    @PostMapping
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> processOrder(@RequestBody @Validated CreateOrderRequest orderRequest) {
        return process(orderRequest)
                .handleAsync(this::handleResponseAsync, executor);
    }

}
