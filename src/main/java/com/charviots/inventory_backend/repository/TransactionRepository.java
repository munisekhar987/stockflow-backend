package com.charviots.inventory_backend.repository;

import com.charviots.inventory_backend.entity.Item;
import com.charviots.inventory_backend.entity.Store;
import com.charviots.inventory_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.item.store = :store ORDER BY t.transactionDate DESC")
    List<Transaction> findByStoreOrderByTransactionDateDesc(@Param("store") Store store);

    List<Transaction> findByItemOrderByTransactionDateDesc(Item item);

    @Query("SELECT t FROM Transaction t WHERE t.item.store = :store AND t.transactionDate >= :startDate ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("store") Store store, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.item.store = :store AND t.transactionDate >= :today")
    long countTodayTransactions(@Param("store") Store store, @Param("today") LocalDateTime today);

    @Query("SELECT SUM(t.quantity) FROM Transaction t WHERE t.item.store = :store AND t.type = 'IN' AND t.transactionDate >= :today")
    Integer sumStockIn(@Param("store") Store store, @Param("today") LocalDateTime today);

    @Query("SELECT SUM(t.quantity) FROM Transaction t WHERE t.item.store = :store AND t.type = 'OUT' AND t.transactionDate >= :today")
    Integer sumStockOut(@Param("store") Store store, @Param("today") LocalDateTime today);
}