package com.ntros.controller.order;

import com.ntros.controller.AbstractApiController;
import com.ntros.converter.order.OrderDataConverter;
import com.ntros.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/orders")
public class OrderDataController extends AbstractApiController {

    private final OrderService orderService;
    private final OrderDataConverter orderDataConverter;

    @Autowired
    public OrderDataController(OrderService orderService, OrderDataConverter orderDataConverter) {
        this.orderService = orderService;
        this.orderDataConverter = orderDataConverter;

    }


    @GetMapping("/all")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> getAllOrders() {
        return orderService.getAllOrdersAsync()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderDataConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);
    }

    @DeleteMapping("/all")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> deleteAllOrders() {
        return orderService.deleteAllOrdersAsync()
                .handleAsync(this::handleResponseAsync, executor);
    }
}
