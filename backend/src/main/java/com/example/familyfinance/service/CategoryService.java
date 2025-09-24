package com.example.familyfinance.service;

import com.example.familyfinance.repository.BudgetRepository;
import com.example.familyfinance.repository.ExpenseRepository;
import com.example.familyfinance.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public List<Map<String, Object>> getCategoryUsage(Long categoryId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 收入
        incomeRepository.findYearMonthByCategory(categoryId)
                .forEach(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", row[0]);
                    map.put("month", row[1]);
                    map.put("source", row[2]); // 收入
                    result.add(map);
                });

        // 支出
        expenseRepository.findYearMonthByCategory(categoryId)
                .forEach(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", row[0]);
                    map.put("month", row[1]);
                    map.put("source", row[2]); // 支出
                    result.add(map);
                });

        // 预算
        budgetRepository.findYearMonthByCategory(categoryId)
                .forEach(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", row[0]);
                    map.put("month", row[1]);
                    map.put("source", row[2]); // 预算
                    result.add(map);
                });

        // 按时间排序
        result.sort(Comparator.comparingInt((Map<String, Object> m) -> (Integer)m.get("year"))
                .thenComparingInt(m -> (Integer)m.get("month")));
        return result;
    }

}
