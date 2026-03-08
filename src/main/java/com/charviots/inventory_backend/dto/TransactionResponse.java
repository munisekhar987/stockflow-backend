package com.charviots.inventory_backend.dto;

import com.charviots.inventory_backend.entity.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private String userName;
    private TransactionType type;  // Use the enum type
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String notes;
    private LocalDateTime transactionDate;
}