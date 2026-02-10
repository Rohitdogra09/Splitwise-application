package com.splitwise.splitwise.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder

@Entity
@Table(name="group_members",
uniqueConstraints = @UniqueConstraint(columnNames = {"group_id","user_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor

public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id")
    private User user;
}
