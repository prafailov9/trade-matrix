package com.ntros.model.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@RequiredArgsConstructor
public class ProductPriceHistory {


    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer priceHistoryId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;

    private OffsetDateTime priceDate;

}
