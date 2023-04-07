package com.saving.accounts.repository;

import com.saving.accounts.model.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface iAccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findOneByAccount(String account);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.amount = ?1 WHERE a.account = ?2")
    void updateAmmountByAccount(Integer amount, String acount);
}
