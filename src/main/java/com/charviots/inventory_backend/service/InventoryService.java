package com.charviots.inventory_backend.service;

import com.charviots.inventory_backend.dto.*;
import com.charviots.inventory_backend.entity.*;
import com.charviots.inventory_backend.entity.User.Role;
import com.charviots.inventory_backend.entity.Transaction.TransactionType;
import com.charviots.inventory_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // Helper method to get current store
    private Store getCurrentStore() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Admin users cannot access inventory directly. Please use store management.");
        }

        if (user.getStore() == null) {
            throw new RuntimeException("User is not assigned to any store");
        }

        return user.getStore();
    }

    // Helper method to get current user
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ==================== CATEGORY METHODS ====================

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Store store = getCurrentStore();
        User currentUser = getCurrentUser();

        if (categoryRepository.existsByNameAndStore(request.getName(), store)) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists in this store");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .store(store)
                .createdBy(currentUser)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        Store store = getCurrentStore();
        return categoryRepository.findByStoreOrderByNameAsc(store).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Store store = getCurrentStore();
        Category category = categoryRepository.findByIdAndStore(id, store)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByNameAndStore(request.getName(), store)) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists in this store");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        return mapToCategoryResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Store store = getCurrentStore();
        Category category = categoryRepository.findByIdAndStore(id, store)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check if category has items
        List<Item> items = itemRepository.findByCategoryOrderByNameAsc(category);
        if (!items.isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing items");
        }

        categoryRepository.delete(category);
    }

    // ==================== ITEM METHODS ====================

    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        Store store = getCurrentStore();
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findByIdAndStore(request.getCategoryId(), store)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Item item = Item.builder()
                .name(request.getName())
                .category(category)
                .store(store)
                .createdBy(currentUser)
                .specifications(request.getSpecifications())
                .currentQuantity(request.getCurrentQuantity() != null ? request.getCurrentQuantity() : 0)
                .unit(request.getUnit())
                .minStockLevel(request.getMinStockLevel())
                .build();

        Item saved = itemRepository.save(item);
        return mapToItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        Store store = getCurrentStore();
        return itemRepository.findByStoreOrderByNameAsc(store).stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByCategory(Long categoryId) {
        Store store = getCurrentStore();
        Category category = categoryRepository.findByIdAndStore(categoryId, store)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return itemRepository.findByCategoryOrderByNameAsc(category).stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        Store store = getCurrentStore();
        return itemRepository.findLowStockItems(store).stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Store store = getCurrentStore();
        Item item = itemRepository.findByIdAndStore(id, store)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Category category = categoryRepository.findByIdAndStore(request.getCategoryId(), store)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        item.setName(request.getName());
        item.setCategory(category);
        item.setSpecifications(request.getSpecifications());
        item.setUnit(request.getUnit());
        item.setMinStockLevel(request.getMinStockLevel());

        // Note: currentQuantity should only be updated via transactions

        Item updated = itemRepository.save(item);
        return mapToItemResponse(updated);
    }

    @Transactional
    public void deleteItem(Long id) {
        Store store = getCurrentStore();
        Item item = itemRepository.findByIdAndStore(id, store)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        itemRepository.delete(item);
    }

    // ==================== TRANSACTION METHODS ====================

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Store store = getCurrentStore();
        User currentUser = getCurrentUser();

        Item item = itemRepository.findByIdAndStore(request.getItemId(), store)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int previousQuantity = item.getCurrentQuantity();
        int newQuantity;

        if (request.getType() == TransactionType.IN) {
            newQuantity = previousQuantity + request.getQuantity();
        } else {
            newQuantity = previousQuantity - request.getQuantity();
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient stock. Current quantity: " + previousQuantity);
            }
        }

        // Update item quantity
        item.setCurrentQuantity(newQuantity);
        itemRepository.save(item);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .item(item)
                .user(currentUser)
                .type(request.getType())
                .quantity(request.getQuantity())
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .notes(request.getNotes())
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToTransactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        Store store = getCurrentStore();
        return transactionRepository.findByStoreOrderByTransactionDateDesc(store).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByItem(Long itemId) {
        Store store = getCurrentStore();
        Item item = itemRepository.findByIdAndStore(itemId, store)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        return transactionRepository.findByItemOrderByTransactionDateDesc(item).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(int days) {
        Store store = getCurrentStore();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return transactionRepository.findRecentTransactions(store, startDate).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // ==================== DASHBOARD METHODS ====================

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        Store store = getCurrentStore();
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        long totalCategories = categoryRepository.countByStore(store);
        long totalItems = itemRepository.countByStore(store);
        long lowStockItems = itemRepository.countLowStockItems(store);
        long todayTransactions = transactionRepository.countTodayTransactions(store, today);

        Integer totalStockIn = transactionRepository.sumStockIn(store, today);
        Integer totalStockOut = transactionRepository.sumStockOut(store, today);

        return DashboardStats.builder()
                .totalCategories(totalCategories)
                .totalItems(totalItems)
                .lowStockItems(lowStockItems)
                .todayTransactions(todayTransactions)
                .totalStockIn(totalStockIn != null ? totalStockIn : 0)
                .totalStockOut(totalStockOut != null ? totalStockOut : 0)
                .build();
    }

    // ==================== MAPPING METHODS ====================

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdBy(category.getCreatedBy().getFullName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .specifications(item.getSpecifications())
                .currentQuantity(item.getCurrentQuantity())
                .unit(item.getUnit())
                .minStockLevel(item.getMinStockLevel())
                .isLowStock(item.isLowStock())
                .createdBy(item.getCreatedBy().getFullName())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .itemId(transaction.getItem().getId())
                .itemName(transaction.getItem().getName())
                .userName(transaction.getUser().getFullName())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .previousQuantity(transaction.getPreviousQuantity())
                .newQuantity(transaction.getNewQuantity())
                .notes(transaction.getNotes())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }
}