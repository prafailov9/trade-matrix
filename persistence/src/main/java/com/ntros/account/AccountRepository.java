package com.ntros.account;

import com.ntros.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query(value = "SELECT a.* FROM account a " +
            "JOIN wallet w ON a.account_id = w.account_id " +
            "GROUP BY a.account_id " +
            "HAVING COUNT(w.wallet_id) = :count", nativeQuery = true)
    List<Account> findAllByWalletCount(@Param("count") int count);

}
