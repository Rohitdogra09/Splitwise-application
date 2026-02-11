package com.splitwise.splitwise.backend.repository;

import com.splitwise.splitwise.backend.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByExpenseId(Long expenseId);

    List<ExpenseSplit> findByExpenseGroupId(Long groupId);
}
