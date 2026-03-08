package com.charviots.inventory_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalCategories;
    private Long totalItems;
    private Long lowStockItems;
    private Long todayTransactions;  // Make sure this field exists
    private Integer totalStockIn;
    private Integer totalStockOut;
}