package com.ntros.service.product;

import com.ntros.model.product.Product;

import java.util.concurrent.CompletableFuture;

public interface ProductService {


    CompletableFuture<Product> getProduct(String isin);


}
