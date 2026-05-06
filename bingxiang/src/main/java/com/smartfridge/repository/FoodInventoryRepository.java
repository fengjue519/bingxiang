package com.smartfridge.repository;

import com.smartfridge.entity.FoodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodInventoryRepository extends JpaRepository<FoodInventory, Long> {
    List<FoodInventory> findByUserIdOrderByExpiryDateAsc(Long userId);

    @Query("SELECT f FROM FoodInventory f WHERE f.userId = :userId AND f.expiryDate <= :date AND f.expiryDate IS NOT NULL ORDER BY f.expiryDate ASC")
    List<FoodInventory> findExpiringItems(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(f) FROM FoodInventory f WHERE f.userId = :userId AND f.expiryDate IS NOT NULL AND f.expiryDate <= :date")
    int countExpiringItems(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(f) FROM FoodInventory f WHERE f.userId = :userId")
    int countByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FoodInventory f WHERE f.userId = :userId AND LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FoodInventory> searchByFoodName(@Param("userId") Long userId, @Param("keyword") String keyword);
}
