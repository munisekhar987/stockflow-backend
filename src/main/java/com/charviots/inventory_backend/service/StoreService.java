package com.charviots.inventory_backend.service;

import com.charviots.inventory_backend.dto.StoreRequest;
import com.charviots.inventory_backend.dto.StoreResponse;
import com.charviots.inventory_backend.entity.Store;
import com.charviots.inventory_backend.entity.User;
import com.charviots.inventory_backend.repository.StoreRepository;
import com.charviots.inventory_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    private void checkAdminAccess() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
    }

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        checkAdminAccess();

        if (storeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Store with this name already exists");
        }

        Store store = Store.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .enabled(true)
                .build();

        store = storeRepository.save(store);
        return toStoreResponse(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::toStoreResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getEnabledStores() {
        log.info("Fetching enabled stores");
        try {
            List<StoreResponse> stores = storeRepository.findAll().stream()
                    .filter(Store::getEnabled)
                    .map(this::toStoreResponse)
                    .collect(Collectors.toList());
            log.info("Found {} enabled stores", stores.size());
            return stores;
        } catch (Exception e) {
            log.error("Error fetching enabled stores: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        checkAdminAccess();

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());

        store = storeRepository.save(store);
        return toStoreResponse(store);
    }

    @Transactional
    public StoreResponse updateStoreStatus(Long id, Boolean enabled) {
        checkAdminAccess();

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setEnabled(enabled);
        store = storeRepository.save(store);

        return toStoreResponse(store);
    }

    @Transactional
    public void deleteStore(Long id) {
        checkAdminAccess();

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        List<User> users = store.getUsers();
        if (users != null && !users.isEmpty()) {
            throw new RuntimeException("Cannot delete store with existing users");
        }

        storeRepository.delete(store);
    }

    private StoreResponse toStoreResponse(Store store) {
        List<User> users = store.getUsers();
        int userCount = (users != null) ? users.size() : 0;

        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .enabled(store.getEnabled())
                .userCount(userCount)
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}