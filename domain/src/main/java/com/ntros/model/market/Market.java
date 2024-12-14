package com.ntros.model.market;

import com.ntros.model.currency.Currency;
import com.ntros.model.product.MarketProduct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer marketId;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private String marketName;

    @Column(nullable = false)
    private String marketCode;

    private BigDecimal marketCap;

    private String country;

    private String timezone;

    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

//    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MarketProduct> productsForMarket;
}
