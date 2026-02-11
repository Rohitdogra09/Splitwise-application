package com.splitwise.splitwise.backend.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name="expense_splits",
uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id","user_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder

public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="expense_id")
    private Expense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id")
    private User user;

    @Column (nullable = false)
    private  double shareAmount;

}
