package com.smartfridge.controller;

import com.smartfridge.entity.Recipe;
import com.smartfridge.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping
    public String recipes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> recommendations = recipeService.getRecommendationsByUserIngredients(userId);
        model.addAttribute("recipes", recommendations);
        return "recipes";
    }

    @GetMapping("/all")
    public String allRecipes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Recipe> recipes = recipeService.getAllRecipes();
        model.addAttribute("allRecipes", recipes);
        return "all_recipes";
    }
}
