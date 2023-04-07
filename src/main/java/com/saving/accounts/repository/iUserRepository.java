package com.saving.accounts.repository;

import com.saving.accounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface iUserRepository extends JpaRepository<User, Integer> {
    Optional<User> findOneByIdentification(String identification);
}
