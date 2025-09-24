package com.example.familyfinance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;        // 成员名字
    private String role;        // 成员角色：父亲、母亲、孩子等
    private String gender;      // 性别
    @Temporal(TemporalType.DATE)
    private Date birthDate;     // 出生日期
    private String phone;       // 联系方式（电话）
    private String remark;      // 备注信息

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 所属用户（家庭）
}
