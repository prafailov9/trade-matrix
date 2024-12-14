package com.ntros.marketproduct;

import com.ntros.model.currency.Currency;
import com.ntros.model.product.MarketProduct;
import com.ntros.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MarketProductRepository extends JpaRepository<MarketProduct, Integer> {

    @Query(value = "SELECT mp FROM MarketProduct mp " +
            "JOIN mp.product p " +
            "JOIN mp.market m " +
            "WHERE p.isin = :isin AND m.marketCode = :code")
    Optional<MarketProduct> findByProductIsinMarketCode(@Param("isin") String isin, @Param("code") String code);

    @Query("SELECT mp.currentPrice From MarketProduct mp " +
            "JOIN mp.market m " +
            "JOIN mp.product p " +
            "WHERE mp.product = :product AND m.currency = :currency")
    Optional<BigDecimal> findMarketPriceForProductCurrency(@Param("product") Product product,
                                                           @Param("currency") Currency currency);

}
