package com.example.familyfinance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
public class Income {
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
