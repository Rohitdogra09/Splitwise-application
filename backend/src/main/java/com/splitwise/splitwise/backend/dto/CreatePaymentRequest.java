package com.splitwise.splitwise.backend.dto;

public record CreatePaymentRequest (
        Long fromUserId,
        Long toUserId,
        double amount
){
}
