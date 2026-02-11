package com.splitwise.splitwise.backend.dto;

import java.util.Map;

public record CreateExpenseRequest (
        String title,
        double amount,
        Long paidByUserId,
        Map<Long, Double> splits
){

}
