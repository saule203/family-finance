package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Account;
import com.example.familyfinance.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public List<Account> getAll(){
        return accountRepository.findAll();
    }

    @PostMapping
    public Account addAccount(@RequestBody Account account) {
        return accountRepository.save(account);
    }

    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);
        return "账户已删除";
    }
}
