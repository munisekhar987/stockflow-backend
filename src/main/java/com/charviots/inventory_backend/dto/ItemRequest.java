package com.charviots.inventory_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Item DTOs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    private String name;
    private Long categoryId;
    private String specifications;
    private Integer currentQuantity;
    private String unit;
    private Integer minStockLevel;
}
