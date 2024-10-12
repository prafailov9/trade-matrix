package com.ntros.portfolio;

import com.ntros.model.account.Account;
import com.ntros.model.portfolio.Portfolio;
import com.ntros.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {

//    @Query("SELECT pf FROM Portfolio pf " +
//            "JOIN pf.account a " +
//            "JOIN pf.position pos" +
//            "JOIN pos.product pr " +
//            "WHERE pf.account = :account AND p = :product")

    @Query(value = """
            SELECT p.*
            FROM portfolio p
            JOIN account a ON p.account_id = a.account_id
            JOIN position pos ON pos.portfolio_id = p.portfolio_id
            JOIN product prod ON pos.product_id = prod.product_id
            WHERE a.account_number = '123456789012'
            AND prod.isin = 'US0378331005';""", nativeQuery = true)
    Optional<Portfolio> findByAccountProductIsin(@Param("account") Account account, @Param("product") Product product);

    @Query(value = """
            SELECT p.*
            FROM portfolio p
            JOIN account a ON p.account_id = a.account_id
            JOIN position pos ON pos.portfolio_id = p.portfolio_id
            JOIN product prod ON pos.product_id = prod.product_id
            WHERE a.account_number = :accountNumber
            AND prod.isin = :isin;""", nativeQuery = true)
    Optional<Portfolio> findByAccountNumberProductIsin(@Param("accountNumber") String accountNumber, @Param("isin") String isin);

}
