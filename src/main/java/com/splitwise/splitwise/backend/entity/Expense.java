package com.splitwise.splitwise.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder
@Getter @Setter
@Table(name="expenses")

public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="group_id")
    private Group group;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private double amount;

    @ManyToOne(optional = false)
    @JoinColumn(name="paid_by")
    private  User paidBy;

    private LocalDateTime createdAt;
}
