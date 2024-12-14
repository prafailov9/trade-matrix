package com.ntros.position;

import com.ntros.model.Position;
import com.ntros.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Integer> {

    @Query("SELECT pos FROM Position pos " +
            "JOIN pos.portfolio pf " +
            "JOIN pos.product pr " +
            "WHERE pf.account.accountNumber = :accountNumber " +
            "AND pr.isin = :isin")
    Optional<Position> findOneByAccountNumberProductIsin(@Param("accountNumber") String accountNumber, @Param("isin") String isin);

    @Query("SELECT pos FROM Position pos " +
            "JOIN pos.portfolio pf " +
            "JOIN pos.product pr " +
            "WHERE pf.account = :account " +
            "AND pr.isin = :isin")
    Optional<Position> findOneByAccountProductIsin(Account account, @Param("isin") String isin);


    // compare available quantity for product on position
    @Query("SELECT CASE WHEN pos.quantity < :orderQuantity THEN false ELSE true END " +
            "FROM Position pos " +
            "JOIN pos.product pr " +
            "JOIN pos.portfolio pf " +
            "WHERE pf.account.accountNumber = :accountNumber AND pr.isin = :isin")
    Optional<Boolean> compareCurrentProductQuantity(@Param("orderQuantity") int orderQuantity, @Param("accountNumber") String accountNumber, @Param("isin") String isin);

    @Query("SELECT pos.quantity FROM Position pos " +
            "JOIN pos.portfolio pf " +
            "JOIN pos.product pr " +
            "WHERE pf.account.accountNumber = :accountNumber AND pr.isin = :isin")
    Optional<Integer> findQuantityByAccountNumberProductIsin(@Param("accountNumber") String accountNumber, @Param("isin") String isin);


}
