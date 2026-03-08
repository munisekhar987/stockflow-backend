package com.charviots.inventory_backend.repository;

import com.charviots.inventory_backend.entity.Store;
import com.charviots.inventory_backend.entity.Category;
import com.charviots.inventory_backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStoreOrderByNameAsc(Store store);
    List<Item> findByCategoryOrderByNameAsc(Category category);
    Optional<Item> findByIdAndStore(Long id, Store store);

    @Query("SELECT i FROM Item i WHERE i.store = :store AND i.currentQuantity <= i.minStockLevel AND i.minStockLevel IS NOT NULL")
    List<Item> findLowStockItems(@Param("store") Store store);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.store = :store")
    long countByStore(@Param("store") Store store);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.store = :store AND i.currentQuantity <= i.minStockLevel AND i.minStockLevel IS NOT NULL")
    long countLowStockItems(@Param("store") Store store);
}