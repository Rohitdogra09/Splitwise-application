package com.splitwise.splitwise.backend.repository;

import com.splitwise.splitwise.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
