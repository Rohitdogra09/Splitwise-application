package com.splitwise.splitwise.backend.dto;

public record SettlementDto(
        Long fromUserId, String fromName,
        Long toUserId, String toName,
        double amount
) {
}
