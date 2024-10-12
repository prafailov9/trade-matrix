package com.ntros.model.currency;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer currencyId;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private String currencyName;

    private boolean isActive;

}
