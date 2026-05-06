package com.smartfridge.controller;

import com.smartfridge.entity.FoodCategory;
import com.smartfridge.entity.FoodInventory;
import com.smartfridge.service.FoodInventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private FoodInventoryService foodInventoryService;

    @GetMapping
    public String inventory(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        model.addAttribute("items", items);
        return "inventory";
    }

    @GetMapping("/add")
    public String addFoodPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<FoodCategory> categories = foodInventoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "add_food";
    }

    @PostMapping("/add")
    public String addFood(HttpSession session,
                          @RequestParam String foodName,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam Double quantity,
                          @RequestParam String unit,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
                          @RequestParam(required = false) String storageLocation,
                          @RequestParam(required = false) String notes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        foodInventoryService.addFood(userId, foodName, categoryId, quantity, unit,
                                    purchaseDate, expiryDate, storageLocation, notes);
        return "redirect:/inventory";
    }

    @PostMapping("/update/{id}")
    public String updateFood(@PathVariable Long id,
                            @RequestParam(required = false) Double quantity,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {
        foodInventoryService.updateFood(id, quantity, expiryDate);
        return "redirect:/inventory";
    }

    @GetMapping("/delete/{id}")
    public String deleteFood(@PathVariable Long id) {
        foodInventoryService.deleteFood(id);
        return "redirect:/inventory";
    }
}
