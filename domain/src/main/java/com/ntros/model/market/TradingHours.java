package com.ntros.model.market;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetTime;

@Entity
@Data
@RequiredArgsConstructor
public class TradingHours {

    @Id
    private Integer marketId;

    @OneToOne(cascade = CascadeType.ALL) // have to explicitly set cascade type
    @MapsId
    @JoinColumn(name = "market_id")
    private Market market;

    @Column(nullable = false)
    private OffsetTime openTime;

    @Column(nullable = false)
    private OffsetTime closeTime;

    @Column(nullable = false)
    private String timezone;

}
