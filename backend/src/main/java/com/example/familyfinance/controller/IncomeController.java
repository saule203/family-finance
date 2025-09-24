package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Expense;
import com.example.familyfinance.entity.Income;
import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.Account;
import com.example.familyfinance.repository.IncomeRepository;
import com.example.familyfinance.repository.CategoryRepository;
import com.example.familyfinance.repository.AccountRepository;
import com.example.familyfinance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    // 获取当前用户的收入记录
    @GetMapping("/user/{userId}")
    public List<Income> getAll(@PathVariable Integer userId) {
        return incomeRepository.findByUserId(userId);
    }

    // 添加收入
    @PostMapping
    public Income addIncome(@RequestBody Map<String, Object> payload) {
        Income income = new Income();
        income.setAmount(Double.valueOf(payload.get("amount").toString()));
        income.setDescription(payload.get("description") != null ? payload.get("description").toString() : null);

        try {
            income.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("date").toString()));
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误：" + payload.get("date"));
        }

        // 分类
        Long categoryId = Long.valueOf(payload.get("categoryId").toString());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        income.setCategory(category);

        // 账户
        Long accountId = Long.valueOf(payload.get("accountId").toString());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        income.setAccount(account);

        // 用户
        Integer userId = Integer.valueOf(payload.get("userId").toString());
        income.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在")));

        return incomeRepository.save(income);
    }

    // 修改收入
    @PutMapping("/{id}")
    public Income updateIncome(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return incomeRepository.findById(id).map(income -> {
            income.setAmount(Double.valueOf(payload.get("amount").toString()));
            income.setDescription(payload.get("description") != null ? payload.get("description").toString() : null);

            try {
                income.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("date").toString()));
            } catch (Exception e) {
                throw new RuntimeException("日期格式错误：" + payload.get("date"));
            }

            Long categoryId = Long.valueOf(payload.get("categoryId").toString());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            income.setCategory(category);

            Long accountId = Long.valueOf(payload.get("accountId").toString());
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("账户不存在"));
            income.setAccount(account);

            Integer userId = Integer.valueOf(payload.get("userId").toString());
            income.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在")));

            return incomeRepository.save(income);
        }).orElseThrow(() -> new RuntimeException("Income not found"));
    }

    // 删除收入
    @DeleteMapping("/{id}")
    public void deleteIncome(@PathVariable Integer id) {
        incomeRepository.deleteById(id);
    }

    private Income mapPayloadToIncome(Income income, Map<String, Object> payload) {
        // 金额
        income.setAmount(Double.valueOf(payload.get("amount").toString()));

        // 描述
        income.setDescription(payload.get("description") != null ? payload.get("description").toString() : null);

        // 日期
        String dateStr = payload.get("date").toString();
        try {
            income.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误：" + dateStr);
        }

        // 分类
        Map<String, Object> catMap = (Map<String, Object>) payload.get("category");
        Long categoryId = Long.valueOf(catMap.get("id").toString());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        income.setCategory(category);

        // 账户
        Map<String, Object> accMap = (Map<String, Object>) payload.get("account");
        Long accountId = Long.valueOf(accMap.get("id").toString());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        income.setAccount(account);

        // 用户
        Map<String, Object> userMap = (Map<String, Object>) payload.get("user");
        Integer userId = Integer.valueOf(userMap.get("id").toString());
        income.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在")));

        return income;
    }


}
