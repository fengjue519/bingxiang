package com.smartfridge.controller;

import com.smartfridge.entity.FoodInventory;
import com.smartfridge.service.FoodInventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private FoodInventoryService foodInventoryService;

    @GetMapping
    public String alerts(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<FoodInventory> expiringItems = foodInventoryService.getExpiringItems(userId, 7);
        model.addAttribute("items", expiringItems);
        return "alerts";
    }
}
