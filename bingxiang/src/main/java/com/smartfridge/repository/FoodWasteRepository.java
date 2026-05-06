package com.smartfridge.repository;

import com.smartfridge.entity.FoodWaste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodWasteRepository extends JpaRepository<FoodWaste, Long> {
    List<FoodWaste> findByUserIdOrderByWasteDateDesc(Long userId);

    @Query("SELECT fw FROM FoodWaste fw WHERE fw.userId = :userId AND fw.wasteDate BETWEEN :start AND :end ORDER BY fw.wasteDate DESC")
    List<FoodWaste> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
