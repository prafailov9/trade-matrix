package com.ntros.converter;

import com.ntros.dto.WalletDTO;
import com.ntros.model.wallet.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletConverter implements Converter<WalletDTO, Wallet> {

    @Override
    public WalletDTO toDTO(Wallet model) {
        WalletDTO form = new WalletDTO();
        form.setCurrencyCode(model.getCurrency().getCurrencyCode());
        form.setAccountNumber(model.getAccount().getAccountNumber());
        form.setBalance(model.getBalance());
        form.setMain(model.isMain());
        return form;
    }

    @Override
    public Wallet toModel(WalletDTO walletDTO) {
        Wallet wallet = new Wallet();
        wallet.setMain(walletDTO.isMain());
        wallet.setBalance(walletDTO.getBalance());
        return wallet;
    }
}
