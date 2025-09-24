package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.Expense;
import com.example.familyfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 查询某分类、某用户在指定年月的支出
    @Query("SELECT e FROM Expense e WHERE e.category = :category AND e.user = :user " +
            "AND FUNCTION('YEAR', e.date) = :year AND FUNCTION('MONTH', e.date) = :month")
    List<Expense> findByCategoryAndUserAndMonth(@Param("category") Category category,
                                                @Param("user") User user,
                                                @Param("year") int year,
                                                @Param("month") int month);

    // 查询某用户的所有支出
    List<Expense> findByUserId(Integer userId);

    // 查询某用户在某个时间区间的支出
    List<Expense> findByUserIdAndDateBetween(Integer userId, Date start, Date end);

    // ✅ 修改后的查询：指定用户 + 分类 + 年月
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category = :category " +
            "AND FUNCTION('YEAR', e.date) = :year AND FUNCTION('MONTH', e.date) = :month")
    List<Expense> findByUserAndCategoryAndYearAndMonth(@Param("userId") Integer userId,
                                                       @Param("category") Category category,
                                                       @Param("year") Integer year,
                                                       @Param("month") Integer month);

    @Query("SELECT DISTINCT FUNCTION('YEAR', e.date) AS year, FUNCTION('MONTH', e.date) AS month, '支出' AS source " +
            "FROM Expense e WHERE e.category.id = :catId")
    List<Object[]> findYearMonthByCategory(@Param("catId") Long categoryId);
}
