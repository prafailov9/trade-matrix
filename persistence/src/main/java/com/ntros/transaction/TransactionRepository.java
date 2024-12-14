package com.ntros.transaction;

import com.ntros.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.portfolio p " +
            "WHERE p.portfolioName = :portfolioName")
    List<Transaction> findAllByPortfolioName(@Param("portfolioName") String portfolioName);

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.wallet w " +
            "JOIN w.account a " +
            "WHERE a.accountNumber = :accountNumber")
    List<Transaction> findAllByAccountNumber(@Param("accountNumber") String accountNumber);


}
