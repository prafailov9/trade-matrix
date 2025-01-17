package com.ntros.controller.order;

import com.ntros.controller.AbstractApiController;
import com.ntros.converter.order.OrderDataConverter;
import com.ntros.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
        return orderService.getAllOrders()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderDataConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);
    }

    @GetMapping("/open")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> getAllOpenOrders() {
        return orderService.getAllOpenOrders()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderDataConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);

    }

    @GetMapping("/filled")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> getAllFilledOrders() {
        return orderService.getAllFilledOrders()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderDataConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);

    }

    @GetMapping("/partial")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> getAllPartialOrders() {
        return orderService.getAllPartialOrders()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderDataConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);

    }
}
