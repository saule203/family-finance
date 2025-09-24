package com.example.familyfinance.repository;

import com.example.familyfinance.entity.Category;
import  org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long>{
    List<Category> findByType(String type);
    List<Category> findByUserId(Integer userId);

    List<Category> findByUserIdAndType(Integer userId, String type);
}
