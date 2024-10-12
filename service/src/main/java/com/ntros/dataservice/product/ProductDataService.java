package com.ntros.dataservice.product;

import com.ntros.exception.ProductNotFoundException;
import com.ntros.model.product.Product;
import com.ntros.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@Slf4j
public class ProductDataService implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductDataService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public CompletableFuture<Product> getProduct(String isin) {
        return CompletableFuture
                .supplyAsync(() -> productRepository.findOneByIsin(isin)
                        .orElseThrow(() -> new ProductNotFoundException(String.format("Product not found for given isin: %s", isin))));
    }
}
