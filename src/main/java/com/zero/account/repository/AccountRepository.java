package com.zero.account.repository;

import com.zero.account.domain.Account;
import com.zero.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Long> countByAccountUserId(Long accountUser_Id);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<List<Account>> findByAccountUserId(Long Id);

}
