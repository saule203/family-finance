package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Category;
import com.example.familyfinance.entity.Income;
import com.example.familyfinance.repository.CategoryRepository;
import com.example.familyfinance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Category> getAll(@PathVariable Integer userId) {
        return categoryRepository.findByUserId(userId);
    }

    @GetMapping("/type/{type}")
    public List<Category> getByType(@PathVariable String type) {
        return categoryRepository.findByType(type);
    }

    @GetMapping("/user/{userId}/type/{type}")
    public List<Category> getByUserAndType(@PathVariable Integer userId,
                                           @PathVariable String type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        existing.setName(category.getName());
        existing.setType(category.getType());
        return categoryRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (DataIntegrityViolationException e) {
            // 外键约束异常
            return ResponseEntity.status(HttpStatus.CONFLICT)  // 409
                    .body("该分类已被使用，无法删除");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/usage")
    public List<Map<String, Object>> getCategoryUsage(@PathVariable Long id) {
        return categoryService.getCategoryUsage(id);
    }
}
