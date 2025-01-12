package com.ntros.converter;

import com.ntros.dto.AccountDTO;
import com.ntros.dto.AccountWalletCountDTO;
import com.ntros.dto.WalletDTO;
import com.ntros.model.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

import static java.lang.String.format;

@Component
public class AccountConverter implements Converter<AccountDTO, Account> {

    private final WalletConverter walletModelConverter;

    @Autowired
    public AccountConverter(final WalletConverter walletModelConverter) {
        this.walletModelConverter = walletModelConverter;
    }

    @Override
    public AccountDTO toDTO(Account model) {
        AccountDTO form = new AccountDTO();
        form.setAccountNumber(model.getAccountNumber());
        form.setAccountOwner(format("%s %s", model.getUser().getFirstName(), model.getUser().getLastName()));
        form.setAccountNumber(model.getAccountNumber());
        form.setTotalBalance(model.getTotalBalance());
        form.setCreatedDate(model.getCreatedDate());

        List<WalletDTO> walletDTOS = model.getWallets().stream().map(walletModelConverter::toDTO).toList();
        form.setWallets(walletDTOS);
        return form;
    }

    @Override
    public Account toModel(AccountDTO accountDTO) {

        Account account = new Account();
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setAccountName(accountDTO.getAccountName());
        account.setCreatedDate(OffsetDateTime.now());
        return account;
    }

    public AccountWalletCountDTO toAccountWalletCountDTO(Account account) {
        AccountWalletCountDTO accountWalletCountDTO = new AccountWalletCountDTO();
        accountWalletCountDTO.setAccountDTO(toDTO(account));
        accountWalletCountDTO.setWalletCount(account.getWallets().size());
        return accountWalletCountDTO;
    }
}
