package com.ntros.transaction;

import com.ntros.model.transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Integer> {


    @Query("SELECT t FROM TransactionType t WHERE t.transactionTypeName = :type")
    Optional<TransactionType> findOneByTransactionTypeName(@Param("type") String type);

}
