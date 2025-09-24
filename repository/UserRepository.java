package com.example.familyfinance.repository;

import com.example.familyfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer>{
    User findByUsername(String name);
}
