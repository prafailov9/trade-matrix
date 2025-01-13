package com.ntros.order;

import com.ntros.model.order.Order;
import com.ntros.model.product.MarketProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM OrderStatus os " +
            "JOIN os.order o " +
            "JOIN o.marketProduct mp " +
            "JOIN mp.product p " +
            "JOIN o.wallet w " +
            "JOIN w.account a " +
            "WHERE os.currentStatus = 'OPEN' AND p.isin = :isin AND a.accountNumber = :accountNumber")
    Optional<Order> findByAccountNumberProductIsinOrderStatus(@Param("accountNumber") String accountNumber,
                                                              @Param("isin") String isin);

    // Finds matching open buy orders for current open sell order and sorts by most recent + highest price
    @Query("SELECT o FROM OrderStatus os " +
            "JOIN os.order o " +
            "WHERE o.marketProduct = :marketProduct " +
            "AND o.side = 'BUY' " +
            "AND o.price <= :sellOrderPrice " +
            "AND o.quantity > 0 " +
            "AND os.currentStatus = 'OPEN' OR os.currentStatus = 'PARTIALLY_FILLED' " +
            "ORDER BY o.price ASC, o.placedAt ASC")
    List<Order> findAllByMatchingBuyOrders(@Param("marketProduct") MarketProduct marketProduct, @Param("sellOrderPrice") BigDecimal sellOrderPrice);

    // Finds sell orders for open buy order, sorts by most recent + lowest prices
    @Query("SELECT o FROM OrderStatus os " +
            "JOIN os.order o " +
            "WHERE o.marketProduct = :marketProduct " +
            "AND o.side = 'SELL' " +
            "AND o.price >= :buyOrderPrice " +
            "AND o.quantity > 0 " +
            "AND os.currentStatus = 'OPEN' OR os.currentStatus = 'PARTIALLY_FILLED' " +
            "ORDER BY o.price ASC, o.placedAt ASC")
    List<Order> findAllByMatchingSellOrders(@Param("marketProduct") MarketProduct marketProduct, @Param("buyOrderPrice") BigDecimal buyOrderPrice);

    @Query("SELECT o FROM OrderStatus os JOIN os.order o WHERE os.currentStatus = 'OPEN'")
    List<Order> findAllOpenOrders();

}

