package com.smartfridge.controller;

import com.smartfridge.entity.FoodCategory;
import com.smartfridge.entity.FoodInventory;
import com.smartfridge.entity.Recipe;
import com.smartfridge.service.FoodInventoryService;
import com.smartfridge.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private FoodInventoryService foodInventoryService;

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        int totalItems = foodInventoryService.getTotalItems(userId);
        int expiringCount = foodInventoryService.getExpiringCount(userId);
        List<FoodInventory> expiringItems = foodInventoryService.getExpiringItems(userId, 3);
        List<Map<String, Object>> recommendations = recipeService.getRecommendationsByUserIngredients(userId);

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("expiringCount", expiringCount);
        model.addAttribute("expiringItems", expiringItems);
        model.addAttribute("recommendations", recommendations.subList(0, Math.min(5, recommendations.size())));

        return "index";
    }
}
