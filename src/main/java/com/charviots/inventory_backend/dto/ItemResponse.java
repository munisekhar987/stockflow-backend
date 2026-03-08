package com.charviots.inventory_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String specifications;
    private Integer currentQuantity;
    private String unit;
    private Integer minStockLevel;
    private Boolean isLowStock;
    private String createdBy;  // Make sure this field exists
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}