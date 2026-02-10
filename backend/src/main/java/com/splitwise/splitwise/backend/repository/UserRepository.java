package com.splitwise.splitwise.backend.repository;

import com.splitwise.splitwise.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
