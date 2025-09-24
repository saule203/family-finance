package com.example.familyfinance.service;

import com.example.familyfinance.entity.Budget;
import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.Expense;
import com.example.familyfinance.entity.User;
import com.example.familyfinance.repository.BudgetRepository;
import com.example.familyfinance.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * 设置预算
     */
    public Budget setBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    /**
     * 判断某分类在某年月是否超支
     */
    public boolean isOverBudget(Category category, User user, int year, int month) {
        Optional<Budget> budgetOpt = budgetRepository.findByUser_IdAndYearAndMonth(user.getId(), year, month)
                .stream()
                .filter(b -> b.getCategory() != null && category != null && b.getCategory().getId().equals(category.getId()))
                .findFirst();

        if (budgetOpt.isEmpty()) {
            return false;
        }

        double totalSpent = expenseRepository.findByCategoryAndUserAndMonth(category, user, year, month)
                .stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                .sum();

        return totalSpent > (budgetOpt.get().getAmount() != null ? budgetOpt.get().getAmount() : 0);
    }

    /**
     * 年度预算和支出统计（按月份汇总）
     */
    public List<Map<String, Object>> getYearlyBudgetAndExpense(Integer userId, int year) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Expense> allExpenses = expenseRepository.findByUserId(userId);

        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;

            // 预算总额
            double budget = budgetRepository.findByUser_IdAndYearAndMonth(userId, year, currentMonth)
                    .stream()
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount() : 0)
                    .sum();

            // 支出总额
            double spent = allExpenses.stream()
                    .filter(e -> e.getDate() != null)
                    .filter(e -> {
                        LocalDate d = new java.sql.Date(e.getDate().getTime()).toLocalDate();
                        return d.getYear() == year && d.getMonthValue() == currentMonth;
                    })
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                    .sum();

            Map<String, Object> data = new HashMap<>();
            data.put("month", currentMonth);
            data.put("budget", budget);
            data.put("spent", spent);
            result.add(data);
        }
        return result;
    }

    /**
     * 月度预算和支出统计（按分类汇总）
     */
    public List<Map<String, Object>> getMonthlyBudgetAndExpenseByCategory(Integer userId, int year, int month) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Budget> budgets = budgetRepository.findByUser_IdAndYearAndMonth(userId, year, month);

        for (Budget b : budgets) {
            if (b.getCategory() == null) continue;

            String categoryName = b.getCategory().getName() != null ? b.getCategory().getName() : "未知分类";
            double budgetAmount = b.getAmount() != null ? b.getAmount() : 0.0;

            List<Expense> expenses = expenseRepository.findByUserAndCategoryAndYearAndMonth(
                    userId, b.getCategory(), year, month);

            double spentAmount = expenses.stream()
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                    .sum();

            Map<String, Object> data = new HashMap<>();
            data.put("category", categoryName);
            data.put("budget", budgetAmount);
            data.put("expense", spentAmount);
            result.add(data);
        }

        // 支出没有预算的分类
        List<Expense> allExpenses = expenseRepository.findByUserId(userId).stream()
                .filter(e -> e.getDate() != null)
                .filter(e -> {
                    LocalDate d = new java.sql.Date(e.getDate().getTime()).toLocalDate();
                    return d.getYear() == year && d.getMonthValue() == month;
                })
                .toList();

        Map<String, Double> expensesWithoutBudget = allExpenses.stream()
                .filter(e -> e.getCategory() != null && budgets.stream()
                        .noneMatch(b -> b.getCategory() != null && b.getCategory().getId().equals(e.getCategory().getId())))
                .collect(Collectors.groupingBy(e -> e.getCategory().getName() != null ? e.getCategory().getName() : "未知分类",
                        Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount() : 0)));

        for (Map.Entry<String, Double> entry : expensesWithoutBudget.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("category", entry.getKey());
            data.put("budget", 0.0);
            data.put("expense", entry.getValue());
            result.add(data);
        }

        return result;
    }
}
