package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Budget;
import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser_Id(Integer userId);
    Optional<Budget> findByCategoryAndUser(Category category, User user);

    List<Budget> findByUser_IdAndYearAndMonth(Integer userId, Integer year, Integer month);

    @Query("SELECT DISTINCT b.year, b.month, '预算' AS source FROM Budget b WHERE b.category.id = :catId")
    List<Object[]> findYearMonthByCategory(@Param("catId") Long categoryId);
}
