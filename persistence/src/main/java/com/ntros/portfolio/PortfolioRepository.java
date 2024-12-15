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

    @Query("SELECT pf FROM Portfolio pf " +
            "JOIN pf.account a " +
            "WHERE a.accountNumber = :accountNumber")
    Optional<Portfolio> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT pf FROM Portfolio pf " +
            "JOIN pf.account a " +
            "WHERE a = :account")
    Optional<Portfolio> findByAccount(Account account);

}
