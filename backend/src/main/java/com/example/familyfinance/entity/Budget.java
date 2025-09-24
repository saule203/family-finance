package com.example.familyfinance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount; // 预算金额

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category; // 对应分类

    @ManyToOne
    private User user;

    private Integer year;
    private Integer month;
}
