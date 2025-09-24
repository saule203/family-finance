package com.example.familyfinance.controller;

import com.example.familyfinance.repository.IncomeRepository;
import com.example.familyfinance.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/charts")
public class YearlyChartsController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/yearly")
    public Map<String, Object> getYearlyData(@RequestParam Integer userId, @RequestParam Integer year) {
        Map<String, Object> result = new HashMap<>();
        List<String> months = new ArrayList<>();
        List<Double> incomeList = new ArrayList<>();
        List<Double> expenseList = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            months.add(m + "月");

            // 当月开始和结束
            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());

            // 收入总额
            Double incomeSum = incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                    .stream().mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0).sum();
            incomeList.add(incomeSum);

            // 支出总额
            Double expenseSum = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                    .stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0).sum();
            expenseList.add(expenseSum);
        }

        result.put("months", months);
        result.put("income", incomeList);
        result.put("expense", expenseList);

        return result;
    }
}
