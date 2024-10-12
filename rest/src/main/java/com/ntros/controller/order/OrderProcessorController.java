package com.ntros.controller.order;

import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public interface OrderProcessorController<T> {

    CompletableFuture<ResponseEntity<?>> process(T orderRequest);

}
