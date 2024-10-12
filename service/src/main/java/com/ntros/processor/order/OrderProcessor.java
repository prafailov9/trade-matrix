package com.ntros.processor.order;


import java.util.concurrent.CompletableFuture;

public interface OrderProcessor<T, R> {

    CompletableFuture<R> processOrder(T orderRequest);

}
