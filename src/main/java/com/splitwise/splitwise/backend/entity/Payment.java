package com.splitwise.splitwise.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="payments")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="group_id")
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name="from_user")
    private User fromUser;

    @ManyToOne(optional = false)
    @JoinColumn(name="to_user")
    private User toUser;

    @Column(nullable = false)
    private double amount;

    private LocalDateTime createdAt;
}
