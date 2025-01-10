package com.ntros.model.wallet;

import com.ntros.model.account.Account;
import com.ntros.model.currency.Currency;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walletId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "currency_id")
    private Currency currency;
    private BigDecimal balance;
    private boolean isMain;

    @Version
    private Integer version;

    public void deductBalance(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void increaseBalance(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
