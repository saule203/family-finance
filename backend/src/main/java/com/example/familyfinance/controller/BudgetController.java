package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Budget;
import com.example.familyfinance.entity.Category;
import com.example.familyfinance.repository.BudgetRepository;
import com.example.familyfinance.repository.CategoryRepository;
import com.example.familyfinance.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetService budgetService;

    // 获取所有预算（可选）
    @GetMapping
    public List<Budget> getAll() {
        return budgetRepository.findAll();
    }

    // 获取指定用户的预算
    @GetMapping("/user/{userId}")
    public List<Budget> getByUser(@PathVariable Integer userId) {
        return budgetRepository.findByUser_Id(userId);
    }

    // 新增或修改预算（关联用户）
    @PostMapping
    public Budget addOrUpdateBudget(@RequestBody Budget budget) {
        if (budget.getUser() == null || budget.getUser().getId() == null) {
            throw new RuntimeException("必须关联用户");
        }
        if (budget.getCategory() == null || budget.getCategory().getId() == null) {
            throw new RuntimeException("必须选择分类");
        }
        if (budget.getYear() == null || budget.getMonth() == null) {
            throw new RuntimeException("必须指定年份和月份");
        }

        // 获取分类对象
        Category category = categoryRepository.findById(budget.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        budget.setCategory(category);

        return budgetRepository.save(budget);
    }

    // 查询用户本月预算
    @GetMapping("/user/{userId}/month/{year}/{month}")
    public List<Budget> getUserMonthlyBudgets(@PathVariable Integer userId,
                                              @PathVariable Integer year,
                                              @PathVariable Integer month) {
        return budgetRepository.findByUser_IdAndYearAndMonth(userId, year, month);
    }

    // 删除预算
    @DeleteMapping("/{id}")
    public String deleteBudget(@PathVariable Long id) {
        budgetRepository.deleteById(id);
        return "预算已删除";
    }

    // 获取年度总预算/支出折线图数据
    @GetMapping("/yearly/{userId}/{year}")
    public List<Map<String, Object>> getYearlyBudgetAndExpense(
            @PathVariable Integer userId,
            @PathVariable Integer year) {
        return budgetService.getYearlyBudgetAndExpense(userId, year);
    }

    // 获取当月分类预算柱形图数据
    @GetMapping("/monthly/{userId}/{year}/{month}")
    public List<Map<String, Object>> getMonthlyBudgetAndExpenseByCategory(
            @PathVariable Integer userId,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return budgetService.getMonthlyBudgetAndExpenseByCategory(userId, year, month);
    }
}
