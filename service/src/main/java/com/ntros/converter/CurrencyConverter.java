package com.ntros.converter;

import com.ntros.dto.CurrencyDTO;
import com.ntros.model.currency.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter implements Converter<CurrencyDTO, Currency> {
    @Override
    public CurrencyDTO toDTO(Currency model) {
        CurrencyDTO currencyDTO = new CurrencyDTO();

        currencyDTO.setCurrencyCode(model.getCurrencyCode());
        currencyDTO.setCurrencyName(model.getCurrencyName());
        currencyDTO.setActive(model.isActive());

        return currencyDTO;
    }

    @Override
    public Currency toModel(CurrencyDTO currencyDTO) {
        Currency currency = new Currency();

        currency.setActive(currencyDTO.isActive());
        currency.setCurrencyCode(currencyDTO.getCurrencyCode());
        currency.setCurrencyName(currencyDTO.getCurrencyName());

        return currency;
    }
}
