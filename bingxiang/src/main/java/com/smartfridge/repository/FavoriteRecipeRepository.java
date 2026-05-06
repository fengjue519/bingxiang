package com.smartfridge.repository;

import com.smartfridge.entity.FavoriteRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, Long> {
    List<FavoriteRecipe> findByUserId(Long userId);
    boolean existsByUserIdAndRecipeName(Long userId, String recipeName);
}
