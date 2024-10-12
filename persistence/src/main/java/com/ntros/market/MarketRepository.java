package com.ntros.market;

import com.ntros.model.currency.Currency;
import com.ntros.model.market.Market;
import com.ntros.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface MarketRepository extends JpaRepository<Market, Integer> {


    @Query("SELECT mp.currentPrice From MarketProduct mp " +
            "JOIN mp.market m " +
            "JOIN mp.product p " +
            "WHERE mp.product = :product AND m.currency = :currency")
    Optional<BigDecimal> findMarketPriceForProductCurrency(@Param("product") Product product,
                                                           @Param("currency") Currency currency);


}
