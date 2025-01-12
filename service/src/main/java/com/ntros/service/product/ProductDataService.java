package com.ntros.service.product;

import com.ntros.exception.NotFoundException;
import com.ntros.model.product.Product;
import com.ntros.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
        return supplyAsync(() -> productRepository.findOneByIsin(isin)
                .orElseThrow(() -> NotFoundException.with(format("Product not found for given isin: %s", isin))));
    }
}
