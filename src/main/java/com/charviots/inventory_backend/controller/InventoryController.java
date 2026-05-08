package com.charviots.inventory_backend.controller;


import com.charviots.inventory_backend.dto.*;
import com.charviots.inventory_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    // Dashboard
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(inventoryService.getDashboardStats());
    }
    
    // Categories
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(inventoryService.createCategory(request));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(inventoryService.getAllCategories());
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id, 
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateCategory(id, request));
    }
    
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        inventoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // Items
    @PostMapping("/items")
    public ResponseEntity<ItemResponse> createItem(@RequestBody ItemRequest request) {
        return ResponseEntity.ok(inventoryService.createItem(request));
    }
    
    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }
    
    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(inventoryService.getItemsByCategory(categoryId));
    }
    
    @GetMapping("/items/low-stock")
    public ResponseEntity<List<ItemResponse>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }
    
    @PutMapping("/items/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id, 
            @RequestBody ItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateItem(id, request));
    }
    
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    
    // Transactions
    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(inventoryService.createTransaction(request));
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(inventoryService.getAllTransactions());
    }
    
    @GetMapping("/transactions/item/{itemId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getTransactionsByItem(itemId));
    }
    
    @GetMapping("/transactions/recent/{days}")
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(@PathVariable int days) {
        return ResponseEntity.ok(inventoryService.getRecentTransactions(days));
    }
}
