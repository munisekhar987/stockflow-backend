package com.charviots.inventory_backend.dto;

import com.charviots.inventory_backend.entity.Transaction.TransactionType;
import lombok.Data;

@Data
public class TransactionRequest {
    private Long itemId;
    private TransactionType type;
    private Integer quantity;
    private String notes;
}