package com.example.familyfinance.controller;

import com.example.familyfinance.entity.User;
import com.example.familyfinance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 查询所有用户（仅调试用）
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已存在");
        }
        User savedUser = userRepository.save(user);

        // 注册成功返回用户基本信息（不返回密码）
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());

        return ResponseEntity.ok(response);
    }

    // 登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User found = userRepository.findByUsername(user.getUsername());
        if (found == null || !found.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }

        // 登录成功返回用户信息（不包含密码）
        Map<String, Object> response = new HashMap<>();
        response.put("id", found.getId());
        response.put("username", found.getUsername());

        return ResponseEntity.ok(response);
    }
}
