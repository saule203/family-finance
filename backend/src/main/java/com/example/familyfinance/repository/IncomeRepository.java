package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Income;
import com.example.familyfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income,Integer>{
    List<Income> findByUserId(Integer userId);
    List<Income> findByUserIdAndDateBetween(Integer userId, Date start, Date end);
    List<Income> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT DISTINCT FUNCTION('YEAR', i.date) AS year, FUNCTION('MONTH', i.date) AS month, '收入' AS source " +
            "FROM Income i WHERE i.category.id = :catId")
    List<Object[]> findYearMonthByCategory(@Param("catId") Long categoryId);
}
