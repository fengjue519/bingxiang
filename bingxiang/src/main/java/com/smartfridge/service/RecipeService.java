package com.smartfridge.service;

import com.smartfridge.entity.Recipe;
import com.smartfridge.entity.FoodInventory;
import com.smartfridge.repository.RecipeRepository;
import com.smartfridge.repository.FoodInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodInventoryRepository foodInventoryRepository;

    @PostConstruct
    public void initDefaultRecipes() {
        if (recipeRepository.count() == 0) {
            List<Object[]> defaultRecipes = new ArrayList<>();

            defaultRecipes.add(new Object[]{
                "番茄炒蛋", "经典家常菜，简单易学，营养丰富",
                "番茄,鸡蛋,葱,盐,油",
                "1. 番茄切块，鸡蛋打散\n2. 热锅凉油，先炒鸡蛋\n3. 另起锅炒番茄\n4. 加入鸡蛋一起翻炒\n5. 加盐调味即可",
                15, "简单"
            });

            defaultRecipes.add(new Object[]{
                "红烧肉", "家常硬菜，肥而不腻，入口即化",
                "五花肉,生抽,老抽,冰糖,姜,八角,料酒",
                "1. 五花肉切块焯水\n2. 炒糖色\n3. 加入肉块翻炒\n4. 加调料和水炖煮\n5. 大火收汁",
                60, "中等"
            });

            defaultRecipes.add(new Object[]{
                "蒜蓉西兰花", "清淡健康的蔬菜料理",
                "西兰花,大蒜,盐,蚝油,油",
                "1. 西兰花切小朵焯水\n2. 大蒜切末\n3. 热锅下油爆香蒜末\n4. 加入西兰花翻炒\n5. 加蚝油和盐调味",
                10, "简单"
            });

            defaultRecipes.add(new Object[]{
                "宫保鸡丁", "川菜经典，酸甜微辣",
                "鸡胸肉,花生,干辣椒,花椒,葱,姜,蒜,生抽,醋,糖",
                "1. 鸡胸肉切丁腌制\n2. 调制酱汁\n3. 热锅下油炸香花椒干辣椒\n4. 加入鸡丁翻炒\n5. 倒入酱汁快速翻炒\n6. 加入花生和葱段",
                25, "中等"
            });

            defaultRecipes.add(new Object[]{
                "清蒸鲈鱼", "保持鱼的原汁原味，鲜嫩可口",
                "鲈鱼,姜,葱,蒸鱼豉油,料酒,盐",
                "1. 鲈鱼处理干净\n2. 鱼身抹盐和料酒\n3. 铺上姜丝\n4. 大火蒸10分钟\n5. 撒上葱丝\n6. 淋上热油和蒸鱼豉油",
                20, "中等"
            });

            defaultRecipes.add(new Object[]{
                "酸辣土豆丝", "下饭神器，酸辣开胃",
                "土豆,干辣椒,花椒,蒜,白醋,盐",
                "1. 土豆切细丝泡水\n2. 热锅下油炸香花椒干辣椒\n3. 加入土豆丝大火快炒\n4. 烹入白醋\n5. 加盐调味即可",
                10, "简单"
            });

            for (Object[] recipeData : defaultRecipes) {
                Recipe recipe = new Recipe();
                recipe.setName((String) recipeData[0]);
                recipe.setDescription((String) recipeData[1]);
                recipe.setIngredients((String) recipeData[2]);
                recipe.setInstructions((String) recipeData[3]);
                recipe.setCookingTime((Integer) recipeData[4]);
                recipe.setDifficulty((String) recipeData[5]);
                recipeRepository.save(recipe);
            }
        }
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<Map<String, Object>> getRecommendationsByUserIngredients(Long userId) {
        List<FoodInventory> userFoods = foodInventoryRepository.findByUserIdOrderByExpiryDateAsc(userId);
        Set<String> userIngredients = new HashSet<>();
        for (FoodInventory food : userFoods) {
            userIngredients.add(food.getFoodName().toLowerCase());
        }

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            String[] recipeIngredients = recipe.getIngredients().split(",");
            int matchCount = 0;
            for (String ingredient : recipeIngredients) {
                String trimmedIngredient = ingredient.trim().toLowerCase();
                for (String userIngredient : userIngredients) {
                    if (userIngredient.contains(trimmedIngredient) || trimmedIngredient.contains(userIngredient)) {
                        matchCount++;
                        break;
                    }
                }
            }

            double matchPercentage = (double) matchCount / recipeIngredients.length * 100;
            if (matchPercentage >= 50) {
                Map<String, Object> recipeMap = new HashMap<>();
                recipeMap.put("id", recipe.getId());
                recipeMap.put("name", recipe.getName());
                recipeMap.put("description", recipe.getDescription());
                recipeMap.put("ingredients", recipe.getIngredients());
                recipeMap.put("instructions", recipe.getInstructions());
                recipeMap.put("cookingTime", recipe.getCookingTime());
                recipeMap.put("difficulty", recipe.getDifficulty());
                recipeMap.put("matchPercentage", Math.round(matchPercentage * 10) / 10.0);
                recommendations.add(recipeMap);
            }
        }

        recommendations.sort((a, b) -> Double.compare((Double) b.get("matchPercentage"), (Double) a.get("matchPercentage")));
        return recommendations;
    }
}
