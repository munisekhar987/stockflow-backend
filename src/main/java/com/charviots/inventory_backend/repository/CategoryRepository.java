package com.charviots.inventory_backend.repository;

import com.charviots.inventory_backend.entity.Category;
import com.charviots.inventory_backend.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStoreOrderByNameAsc(Store store);

    Optional<Category> findByIdAndStore(Long id, Store store);

    boolean existsByNameAndStore(String name, Store store);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.store = :store")
    long countByStore(@Param("store") Store store);
}