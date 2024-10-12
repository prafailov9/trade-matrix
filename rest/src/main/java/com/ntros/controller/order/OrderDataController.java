package com.ntros.controller.order;

import com.ntros.controller.AbstractApiController;
import com.ntros.converter.order.OrderConverter;
import com.ntros.dataservice.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/orders")
public class OrderDataController extends AbstractApiController {

    private final OrderService orderService;
    private final OrderConverter orderConverter;

    @Autowired
    public OrderDataController(OrderService orderService, OrderConverter orderConverter) {
        this.orderService = orderService;
        this.orderConverter = orderConverter;

    }


    @GetMapping("/all")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> getAllOrders() {
        return orderService.getAllOrders()
                .thenApplyAsync(orders -> orders.stream()
                        .map(orderConverter::toDTO)
                        .collect(Collectors.toList()), executor)
                .handleAsync(this::handleResponseAsync, executor);
    }

    @DeleteMapping("/all")
    @ResponseBody
    public CompletableFuture<ResponseEntity<?>> deleteAllOrders() {
        return orderService.deleteAllOrders().handleAsync(this::handleResponseAsync, executor);
    }
}
