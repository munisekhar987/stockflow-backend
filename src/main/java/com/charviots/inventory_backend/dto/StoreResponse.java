package com.charviots.inventory_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Boolean enabled;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}