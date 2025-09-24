package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
}
