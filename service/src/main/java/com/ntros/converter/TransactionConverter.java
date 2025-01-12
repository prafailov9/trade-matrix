package com.ntros.converter;

import com.ntros.dto.TransactionDTO;
import com.ntros.model.transaction.Transaction;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class TransactionConverter implements Converter<TransactionDTO, Transaction> {
    @Override
    public TransactionDTO toDTO(Transaction model) {
        TransactionDTO dto = new TransactionDTO();

        dto.setCurrency(model.getCurrency());
        dto.setDate(model.getTransactionDate().toString());
        dto.setPrice(model.getPrice());
        dto.setQuantity(model.getQuantity());

        dto.setAccName(model.getWallet().getAccount().getAccountName());
        dto.setAccNum(model.getWallet().getAccount().getAccountNumber());
        dto.setProdName(model.getMarketProduct().getProduct().getProductName());
        dto.setProdIsin(model.getMarketProduct().getProduct().getIsin());
        dto.setMarketCode(model.getMarketProduct().getMarket().getMarketCode());

        dto.setTransactionType(model.getTransactionType().getTransactionTypeName());
        dto.setTxGenName(format("%s_%s_%s_%s", dto.getAccNum(),
                dto.getAccName(),
                dto.getMarketCode(),
                dto.getProdName()));

        return dto;
    }

    @Override
    public Transaction toModel(TransactionDTO dto) {
        return null;
    }
}
