package com.example.familyfinance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String description;

    @Temporal(TemporalType.DATE)
    private Date date;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Account account;

    @ManyToOne
    private User user;
}
