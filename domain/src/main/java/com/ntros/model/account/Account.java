package com.ntros.model.account;

import com.ntros.model.User;
import com.ntros.model.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "account_number", nullable = false, length = 12, unique = true)
    private String accountNumber;

    @Column(name = "total_balance", nullable = false)
    private BigDecimal totalBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance", nullable = false)
    private RiskTolerance riskTolerance;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = CascadeType.ALL)
    private List<Wallet> wallets;

}
