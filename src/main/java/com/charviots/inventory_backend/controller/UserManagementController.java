package com.charviots.inventory_backend.controller;

import com.charviots.inventory_backend.dto.UpdateUserStatusRequest;
import com.charviots.inventory_backend.dto.UserResponse;
import com.charviots.inventory_backend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userManagementService.updateUserStatus(userId, request.getEnabled()));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")  // Changed from SUPER_ADMIN
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}