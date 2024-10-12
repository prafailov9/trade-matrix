package com.ntros.dataservice.currency;

import com.ntros.model.currency.Currency;
import com.ntros.model.currency.CurrencyExchangeRate;

import java.math.BigDecimal;

public interface CurrencyExchangeRateService {

    CurrencyExchangeRate getExchangeRate(final Currency source, final Currency target);

    BigDecimal convert(final BigDecimal amount, final Currency source, final Currency target);

    BigDecimal convert(final BigDecimal amount, final String source, final String target);

}
