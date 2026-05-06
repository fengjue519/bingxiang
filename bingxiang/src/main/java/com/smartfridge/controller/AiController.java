package com.smartfridge.controller;

import com.smartfridge.entity.FoodInventory;
import com.smartfridge.entity.FavoriteRecipe;
import com.smartfridge.entity.ShoppingItem;
import com.smartfridge.entity.FoodWaste;
import com.smartfridge.repository.FavoriteRecipeRepository;
import com.smartfridge.repository.ShoppingItemRepository;
import com.smartfridge.repository.FoodWasteRepository;
import com.smartfridge.service.DoubaoAiService;
import com.smartfridge.service.FoodInventoryService;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private DoubaoAiService aiService;

    @Autowired
    private FoodInventoryService foodInventoryService;

    @Autowired
    private FavoriteRecipeRepository favoriteRecipeRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private FoodWasteRepository foodWasteRepository;

    @GetMapping("/scan")
    public String scanPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "scan";
    }

    @PostMapping("/scan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanFood(
            @RequestParam("image") MultipartFile image,
            HttpSession session) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置，请在application.properties中设置doubao.api.key");
            return ResponseEntity.ok(result);
        }

        String base64Image = Base64.encodeBase64String(image.getBytes());

        String systemPrompt = "你是一个食材识别专家。请识别图片中的食材，并以JSON格式返回结果。" +
                "格式要求：{\"foods\": [{\"name\": \"食材名\", \"category\": \"分类(新鲜蔬菜/新鲜水果/肉类/海鲜/乳制品/饮料/调味品/其他)\", \"estimated_shelf_life_days\": 保鲜天数, \"storage_location\": \"存放位置(冷藏室/冷冻室/保鲜层)\", \"tips\": \"保鲜建议\"}]}";

        String aiResponse = aiService.chatWithImage(systemPrompt, "请识别这张图片中的所有食材，给出名称、分类、预估保鲜天数、存放位置和保鲜建议", base64Image);

        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/shelf-life")
    public String shelfLifePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "shelf_life";
    }

    @PostMapping("/shelf-life")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> estimateShelfLife(
            @RequestParam String foodName,
            @RequestParam(required = false) String storageLocation,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        String systemPrompt = "你是一个食品安全专家。请根据食材名称和存放条件，给出详细的保鲜信息。" +
                "以JSON格式返回：{\"food_name\": \"食材名\", \"shelf_life_days\": 保鲜天数, " +
                "\"storage_tips\": \"保鲜建议\", \"warning_signs\": \"变质信号\", " +
                "\"best_temperature\": \"最佳温度\", \"nutrition_tips\": \"营养提示\"}";

        String userMsg = "食材：" + foodName + "，存放位置：" + (storageLocation != null ? storageLocation : "未指定") +
                "。请给出保鲜天数和详细建议。";

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recipes")
    public String aiRecipesPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "ai_recipes";
    }

    @PostMapping("/recipes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateRecipes(
            @RequestParam(required = false) String preferences,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        String ingredientList = items.stream()
                .map(f -> f.getFoodName() + "(" + f.getQuantity() + f.getUnit() + ")")
                .collect(Collectors.joining("、"));

        String systemPrompt = "你是一个专业厨师和营养师。根据用户冰箱中的食材，推荐合适的菜谱。" +
                "以JSON数组格式返回：[{\"name\": \"菜名\", \"difficulty\": \"难度(简单/中等/困难)\", " +
                "\"cooking_time\": 烹饪时间(分钟), \"ingredients\": \"所需食材及用量\", " +
                "\"seasonings\": \"所需调料\", \"steps\": \"详细做法步骤\", " +
                "\"nutrition\": \"营养价值\", \"tips\": \"烹饪小贴士\"}]";

        String userMsg = "我冰箱里有这些食材：" + ingredientList;
        if (preferences != null && !preferences.isEmpty()) {
            userMsg += "。我的偏好：" + preferences;
        }
        userMsg += "。请推荐3道菜，包含详细做法、所需食材、调料和营养价值。";

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/daily-plan")
    public String dailyPlanPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "daily_plan";
    }

    @PostMapping("/daily-plan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateDailyPlan(
            @RequestParam(required = false) String preferences,
            @RequestParam(required = false) int peopleCount,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        String ingredientList = items.stream()
                .map(f -> f.getFoodName() + "(" + f.getQuantity() + f.getUnit() + ")")
                .collect(Collectors.joining("、"));

        String systemPrompt = "你是一个专业的家庭营养师和厨师。请为用户生成今日三餐菜谱。" +
                "以JSON格式返回：{\"breakfast\": {\"name\": \"早餐菜名\", \"ingredients\": \"食材\", " +
                "\"seasonings\": \"调料\", \"steps\": \"做法\", \"nutrition\": \"营养分析\"}, " +
                "\"lunch\": {同上}, \"dinner\": {同上}, " +
                "\"shopping_list\": [{\"name\": \"需要购买的食材\", \"quantity\": \"数量\", \"reason\": \"原因\"}], " +
                "\"nutrition_summary\": \"今日营养总结\", \"calories_estimate\": \"预计总热量\"}";

        String userMsg = "我冰箱里有这些食材：" + ingredientList +
                "。用餐人数：" + (peopleCount > 0 ? peopleCount : 2) + "人";
        if (preferences != null && !preferences.isEmpty()) {
            userMsg += "。饮食偏好/限制：" + preferences;
        }
        userMsg += "。请生成今日三餐菜谱，包含详细做法、所需食材调料，并列出需要额外购买的食材清单。";

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/nutrition")
    public String nutritionPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "nutrition";
    }

    @PostMapping("/nutrition")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeNutrition(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        String ingredientList = items.stream()
                .map(f -> f.getFoodName() + "(" + f.getQuantity() + f.getUnit() + ")")
                .collect(Collectors.joining("、"));

        String systemPrompt = "你是一个营养学专家。请分析用户冰箱中食材的营养构成。" +
                "以JSON格式返回：{\"total_calories\": \"总热量估算\", " +
                "\"nutrition_breakdown\": {\"protein\": \"蛋白质\", \"fat\": \"脂肪\", \"carbs\": \"碳水\", " +
                "\"vitamins\": \"维生素\", \"minerals\": \"矿物质\"}, " +
                "\"health_score\": 健康评分(1-100), \"suggestions\": [\"改善建议\"], " +
                "\"missing_nutrients\": [\"缺乏的营养素\"], \"recommended_foods\": [\"建议补充的食物\"]}";

        String userMsg = "请分析以下食材的营养构成并给出建议：" + ingredientList;

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/shopping-list")
    public String shoppingListPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<ShoppingItem> items = shoppingItemRepository.findByUserIdAndPurchasedFalse(userId);
        model.addAttribute("items", items);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "shopping_list";
    }

    @PostMapping("/shopping-list/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateShoppingList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        List<FoodInventory> items = foodInventoryService.getInventoryByUserId(userId);
        String ingredientList = items.stream()
                .map(f -> f.getFoodName() + "(" + f.getQuantity() + f.getUnit() + ")")
                .collect(Collectors.joining("、"));

        String systemPrompt = "你是一个家庭采购专家。根据用户现有食材，推荐需要补充的食材。" +
                "以JSON数组格式返回：[{\"name\": \"食材名\", \"quantity\": \"建议购买量\", " +
                "\"reason\": \"购买原因\", \"priority\": \"优先级(高/中/低)\"}]";

        String userMsg = "我冰箱里现有：" + ingredientList + "。请推荐我需要补充购买的食材，考虑日常营养均衡。";

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/shopping-list/add")
    public String addShoppingItem(@RequestParam String itemName,
                                  @RequestParam(required = false) Double quantity,
                                  @RequestParam(required = false) String unit,
                                  HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        ShoppingItem item = new ShoppingItem();
        item.setUserId(userId);
        item.setItemName(itemName);
        item.setQuantity(quantity != null ? quantity : 1.0);
        item.setUnit(unit != null ? unit : "个");
        item.setPurchased(false);
        shoppingItemRepository.save(item);
        return "redirect:/ai/shopping-list";
    }

    @GetMapping("/shopping-list/purchase/{id}")
    public String purchaseItem(@PathVariable Long id) {
        ShoppingItem item = shoppingItemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setPurchased(true);
            shoppingItemRepository.save(item);
        }
        return "redirect:/ai/shopping-list";
    }

    @GetMapping("/shopping-list/delete/{id}")
    public String deleteShoppingItem(@PathVariable Long id) {
        shoppingItemRepository.deleteById(id);
        return "redirect:/ai/shopping-list";
    }

    @PostMapping("/favorite-recipe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveFavoriteRecipe(
            @RequestParam String recipeName,
            @RequestParam String content,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (favoriteRecipeRepository.existsByUserIdAndRecipeName(userId, recipeName)) {
            result.put("success", false);
            result.put("message", "该菜谱已收藏");
            return ResponseEntity.ok(result);
        }

        FavoriteRecipe favorite = new FavoriteRecipe();
        favorite.setUserId(userId);
        favorite.setRecipeName(recipeName);
        favorite.setContent(content);
        favoriteRecipeRepository.save(favorite);

        result.put("success", true);
        result.put("message", "收藏成功");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/favorites")
    public String favoritesPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FavoriteRecipe> favorites = favoriteRecipeRepository.findByUserId(userId);
        model.addAttribute("favorites", favorites);
        return "favorites";
    }

    @GetMapping("/favorites/delete/{id}")
    public String deleteFavorite(@PathVariable Long id) {
        favoriteRecipeRepository.deleteById(id);
        return "redirect:/ai/favorites";
    }

    @GetMapping("/waste")
    public String wastePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<FoodWaste> wastes = foodWasteRepository.findByUserIdOrderByWasteDateDesc(userId);
        model.addAttribute("wastes", wastes);
        model.addAttribute("aiConfigured", aiService.isConfigured());
        return "waste";
    }

    @PostMapping("/waste")
    public String addWaste(@RequestParam String foodName,
                          @RequestParam Double quantity,
                          @RequestParam String unit,
                          @RequestParam(required = false) String reason,
                          HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        FoodWaste waste = new FoodWaste();
        waste.setUserId(userId);
        waste.setFoodName(foodName);
        waste.setQuantity(quantity);
        waste.setUnit(unit);
        waste.setWasteDate(LocalDate.now());
        waste.setReason(reason);
        foodWasteRepository.save(waste);
        return "redirect:/ai/waste";
    }

    @PostMapping("/waste/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeWaste(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (!aiService.isConfigured()) {
            result.put("success", false);
            result.put("message", "AI服务未配置");
            return ResponseEntity.ok(result);
        }

        List<FoodWaste> wastes = foodWasteRepository.findByUserIdOrderByWasteDateDesc(userId);
        String wasteList = wastes.stream()
                .map(w -> w.getFoodName() + "(" + w.getQuantity() + w.getUnit() + ", 原因:" + (w.getReason() != null ? w.getReason() : "未记录") + ")")
                .collect(Collectors.joining("、"));

        String systemPrompt = "你是一个减少食物浪费的专家。请分析用户的食物浪费记录，给出减少浪费的建议。" +
                "以JSON格式返回：{\"waste_analysis\": \"浪费分析\", \"main_reasons\": [\"主要原因\"], " +
                "\"suggestions\": [\"减少浪费的建议\"], \"shopping_tips\": [\"购物建议\"], " +
                "\"storage_tips\": [\"储存建议\"]}";

        String userMsg = "我的食物浪费记录：" + wasteList + "。请分析并给出减少浪费的建议。";

        String aiResponse = aiService.chat(systemPrompt, userMsg);
        result.put("success", true);
        result.put("aiResponse", aiResponse);
        return ResponseEntity.ok(result);
    }
}
