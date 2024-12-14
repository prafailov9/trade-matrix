package com.ntros.market;

import com.ntros.model.currency.Currency;
import com.ntros.model.market.Market;
import com.ntros.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Integer> {


    @Query("SELECT m FROM Market m WHERE m.marketCode = :marketCode")
    Optional<Market> findByMarketCode(@Param("marketCode") String marketCode);

}
