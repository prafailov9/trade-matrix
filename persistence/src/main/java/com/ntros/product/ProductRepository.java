package com.ntros.product;

import com.ntros.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findOneByIsin(String isin);

}
