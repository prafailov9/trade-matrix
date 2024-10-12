package com.ntros.model.portfolio;

import com.ntros.model.account.Account;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Entity
@Table(name = "portfolio")
@Data
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer portfolioId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "portfolio_name", nullable = false, length = 50)
    private String portfolioName;

    @Column(name = "total_value", nullable = false)
    private double totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "using_margin", nullable = false)
    private Boolean usingMargin;

    private Double marginRatio;

    private Double maxDrawdown;

    private Double sharpeRatio;

    private String investmentGoal;

    private String investmentHorizon;

    @Column(name = "created_date", updatable = false)
    private OffsetDateTime createdDate;

    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;
}


