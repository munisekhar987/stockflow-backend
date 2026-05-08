package com.charviots.inventory_backend.controller;

import com.charviots.inventory_backend.dto.StoreRequest;
import com.charviots.inventory_backend.dto.StoreResponse;
import com.charviots.inventory_backend.dto.UpdateUserStatusRequest;
import com.charviots.inventory_backend.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<StoreResponse>> getEnabledStores() {
        return ResponseEntity.ok(storeService.getEnabledStores());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<StoreResponse> updateStoreStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(storeService.updateStoreStatus(id, request.getEnabled()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}