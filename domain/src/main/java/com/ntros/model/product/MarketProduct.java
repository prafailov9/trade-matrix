package com.ntros.model.product;

import com.ntros.model.market.Market;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "market_product")
@Data
@RequiredArgsConstructor
public class MarketProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer marketProductId;

    @ManyToOne
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "listing_date", updatable = false)
    private OffsetDateTime listingDate;

    @Column(name = "avg_daily_volume", nullable = false)
    private double avgDailyVolume;

    @Column(name = "bid_ask_spread", nullable = false)
    private double bidAskSpread;
}
