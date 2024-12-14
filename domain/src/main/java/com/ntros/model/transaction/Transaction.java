package com.ntros.model.transaction;

import com.ntros.model.order.Order;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"wallet", "order", "product", "portfolio"})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @ManyToOne
    @JoinColumn(name = "transaction_type_id", nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "market_product_id", nullable = false)
    private MarketProduct marketProduct;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 3)
    private String currency;

    @Column(name = "transaction_date", updatable = false)
    private Timestamp transactionDate;
}

