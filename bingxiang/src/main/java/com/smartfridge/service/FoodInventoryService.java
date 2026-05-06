package com.smartfridge.service;

import com.smartfridge.entity.FoodCategory;
import com.smartfridge.entity.FoodInventory;
import com.smartfridge.repository.FoodCategoryRepository;
import com.smartfridge.repository.FoodInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FoodInventoryService {

    @Autowired
    private FoodInventoryRepository foodInventoryRepository;

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @PostConstruct
    public void initDefaultCategories() {
        if (foodCategoryRepository.count() == 0) {
            String[][] defaultCategories = {
                {"新鲜蔬菜", "冷藏室", "2", "8"},
                {"新鲜水果", "冷藏室", "3", "8"},
                {"肉类", "冷冻室", "-18", "-15"},
                {"海鲜", "冷冻室", "-18", "-15"},
                {"乳制品", "冷藏室", "2", "6"},
                {"饮料", "冷藏室", "2", "8"},
                {"调味品", "冷藏室", "0", "10"},
                {"剩菜剩饭", "冷藏室", "2", "6"},
                {"其他", "冷藏室", "0", "10"}
            };

            for (String[] cat : defaultCategories) {
                FoodCategory category = new FoodCategory();
                category.setName(cat[0]);
                category.setStorageLocation(cat[1]);
                category.setOptimalTemperatureMin(Double.parseDouble(cat[2]));
                category.setOptimalTemperatureMax(Double.parseDouble(cat[3]));
                foodCategoryRepository.save(category);
            }
        }
    }

    public FoodInventory addFood(Long userId, String foodName, Long categoryId,
                                 Double quantity, String unit, LocalDate purchaseDate,
                                 LocalDate expiryDate, String storageLocation, String notes) {
        FoodInventory food = new FoodInventory();
        food.setUserId(userId);
        food.setFoodName(foodName);
        food.setCategoryId(categoryId);
        food.setQuantity(quantity);
        food.setUnit(unit);
        food.setPurchaseDate(purchaseDate);
        food.setExpiryDate(expiryDate);
        food.setStorageLocation(storageLocation);
        food.setNotes(notes);
        return foodInventoryRepository.save(food);
    }

    public List<FoodInventory> getInventoryByUserId(Long userId) {
        return foodInventoryRepository.findByUserIdOrderByExpiryDateAsc(userId);
    }

    public List<FoodInventory> getExpiringItems(Long userId, int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return foodInventoryRepository.findExpiringItems(userId, futureDate);
    }

    public int getExpiringCount(Long userId) {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        return foodInventoryRepository.countExpiringItems(userId, futureDate);
    }

    public int getTotalItems(Long userId) {
        return foodInventoryRepository.countByUserId(userId);
    }

    public Optional<FoodInventory> getFoodById(Long id) {
        return foodInventoryRepository.findById(id);
    }

    public FoodInventory updateFood(Long id, Double quantity, LocalDate expiryDate) {
        Optional<FoodInventory> foodOpt = foodInventoryRepository.findById(id);
        if (foodOpt.isPresent()) {
            FoodInventory food = foodOpt.get();
            if (quantity != null) {
                food.setQuantity(quantity);
            }
            if (expiryDate != null) {
                food.setExpiryDate(expiryDate);
            }
            return foodInventoryRepository.save(food);
        }
        return null;
    }

    public void deleteFood(Long id) {
        foodInventoryRepository.deleteById(id);
    }

    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAll();
    }
}
