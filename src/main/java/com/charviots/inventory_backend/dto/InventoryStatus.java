package com.charviots.inventory_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStatus {
    private String categoryName;
    private String itemName;
    private String specifications;
    private Integer currentQuantity;
    private String unit;
    private Integer minStockLevel;
    private Boolean isLowStock;
}
