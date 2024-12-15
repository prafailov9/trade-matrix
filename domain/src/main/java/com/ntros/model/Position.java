package com.ntros.model;

import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer positionId;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    @Version
    private Integer version;

}
