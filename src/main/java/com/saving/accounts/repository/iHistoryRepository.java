package com.saving.accounts.repository;

import com.saving.accounts.model.History;
import com.saving.accounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface iHistoryRepository extends JpaRepository<History, Integer> {
    Optional<List<History>> findAllByAccount(String account);
}
