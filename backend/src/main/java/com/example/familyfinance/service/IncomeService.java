package com.example.familyfinance.service;

import com.example.familyfinance.entity.Income;
import com.example.familyfinance.entity.User;
import com.example.familyfinance.repository.IncomeRepository;
import com.example.familyfinance.repository.MemberRepository;
import com.example.familyfinance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;


    /* ================= 人均收入计算 ================= */

    /**
     * 去年人均收入
     */
    public Double calculatePerCapitaIncomeLastYear(Integer userId) {
        return calculatePerCapitaIncome(userId, "lastYear");
    }

    /**
     * 今年人均收入
     */
    public Double calculatePerCapitaIncomeThisYear(Integer userId) {
        return calculatePerCapitaIncome(userId, "thisYear");
    }

    /**
     * 近12个月人均收入
     */
    public Double calculatePerCapitaIncomeLast12Months(Integer userId) {
        return calculatePerCapitaIncome(userId, "last12");
    }

    /**
     * 统一计算逻辑
     */
    private Double calculatePerCapitaIncome(Integer userId, String mode) {
        User user = userRepository.findById(userId).orElseThrow();
        int population = memberRepository.findByUser(user).size();
        if (population == 0) return 0.0;

        LocalDate start;
        LocalDate end = LocalDate.now();

        if ("lastYear".equals(mode)) {
            int year = Year.now().getValue() - 1;
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        } else if ("thisYear".equals(mode)) {
            int year = Year.now().getValue();
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        } else { // last12
            start = end.minusMonths(12);
        }

        double totalIncome = incomeRepository
                .findByUserAndDateBetween(user, start, end)
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();

        return totalIncome / population;
    }
}
