package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Account;
import com.example.familyfinance.entity.Budget;
import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.Expense;
import com.example.familyfinance.repository.*;
import com.example.familyfinance.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    // 获取某用户所有支出
    @GetMapping("/user/{userId}")
    public List<Expense> getAll(@PathVariable Integer userId) {
        return expenseRepository.findByUserId(userId);
    }

    // 添加支出
    @PostMapping
    public Map<String, Object> addExpense(@RequestBody Map<String, Object> payload) {
        Expense expense = mapPayloadToExpense(new Expense(), payload);
        Expense saved = expenseRepository.save(expense);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("expense", saved);

        int year = expense.getDate().getYear() + 1900;
        int month = expense.getDate().getMonth() + 1;

        boolean overBudget = budgetService.isOverBudget(saved.getCategory(), saved.getUser(), year, month);
        response.put("overBudget", overBudget);

        if (overBudget) {
            response.put("message", "⚠ 分类 [" + saved.getCategory().getName() + "] 已超预算！");
        } else {
            // 检查本月是否有预算
            List<Budget> monthBudgets = budgetRepository.findByUser_IdAndYearAndMonth(saved.getUser().getId(), year, month);
            boolean hasBudget = monthBudgets.stream().anyMatch(b -> b.getCategory().getId().equals(saved.getCategory().getId()));
            if (!hasBudget) {
                response.put("message", "⚠ 本月未设置 [" + saved.getCategory().getName() + "] 分类预算，可自由支出");
            }
        }

        return response;
    }


    // 修改支出
    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return expenseRepository.findById(id).map(expense -> {
            Expense updated = mapPayloadToExpense(expense, payload);
            return expenseRepository.save(updated);
        }).orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    // 删除支出
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseRepository.deleteById(id);
    }

    // 分类汇总
    @GetMapping("/summary/user/{userId}")
    public Map<String, Double> getCategorySummary(@PathVariable Integer userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    private Expense mapPayloadToExpense(Expense expense, Map<String, Object> payload) {
        // 金额
        expense.setAmount(Double.valueOf(payload.get("amount").toString()));

        // 描述
        expense.setDescription(payload.get("description") != null ? payload.get("description").toString() : null);

        // 日期
        String dateStr = payload.get("date").toString();
        try {
            expense.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误：" + dateStr);
        }

        // 分类
        Map<String, Object> catMap = (Map<String, Object>) payload.get("category");
        Long categoryId = Long.valueOf(catMap.get("id").toString());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        expense.setCategory(category);

        // 账户
        Map<String, Object> accMap = (Map<String, Object>) payload.get("account");
        Long accountId = Long.valueOf(accMap.get("id").toString());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        expense.setAccount(account);

        // 用户
        Map<String, Object> userMap = (Map<String, Object>) payload.get("user");
        Integer userId = Integer.valueOf(userMap.get("id").toString());
        expense.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在")));

        return expense;
    }
}
