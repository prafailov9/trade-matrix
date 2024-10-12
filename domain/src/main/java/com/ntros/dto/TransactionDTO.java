package com.ntros.dto;

import com.ntros.model.transaction.TransactionType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class TransactionDTO {

    private TransactionType transactionType;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private BigDecimal fees;
    private String currency;
    private String description;

}
