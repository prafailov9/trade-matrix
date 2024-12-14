package com.ntros.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class TransactionDTO {

    private String transactionType;
    private String accNum;
    private String accName;
    private String prodName;
    private String prodIsin;
    private String marketCode;
    private String txGenName; // generated name for transaction: accountNumber:accountName-marketCode_productName

    private BigDecimal price;
    private int quantity;
    private String currency;
    private String description;
    private String date;


}
