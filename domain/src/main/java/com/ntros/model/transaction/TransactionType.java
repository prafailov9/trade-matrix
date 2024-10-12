package com.ntros.model.transaction;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "transaction_type")
@Data
public class TransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionTypeId;

    @Column(name = "transaction_type_name", nullable = false, length = 32, unique = true)
    private String transactionTypeName;
}
