package com.ntros.model.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "isin", nullable = false, unique = true, length = 12)
    private String isin;

    @Column(name = "standard_deviation")
    private Double standardDeviation;

    @Column(name = "volatile_coefficient")
    private Double volatileCoefficient;

    @Column(name = "description", length = 255)
    private String description;
}
