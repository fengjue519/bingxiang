package com.smartfridge.repository;

import com.smartfridge.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByUserIdAndPurchasedFalse(Long userId);
    List<ShoppingItem> findByUserId(Long userId);
    void deleteByUserIdAndPurchasedTrue(Long userId);
}
