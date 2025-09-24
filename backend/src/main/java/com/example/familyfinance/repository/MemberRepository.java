package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Member;
import com.example.familyfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    List<Member> findByUser(User user);
}
