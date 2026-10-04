package com.selling.repository;

import com.selling.model.DailyCount;
import com.selling.model.DailyCountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCountDetailsRepo extends JpaRepository<DailyCountDetails, Integer> {

	@Query("SELECT dcd FROM DailyCountDetails dcd WHERE dcd.dailyCount = :dailyCount AND dcd.productId = :productId")
	List<DailyCountDetails> findByDailyCountAndProductId(@Param("dailyCount") DailyCount dailyCount, @Param("productId") Integer productId);

	@Query("SELECT dcd FROM DailyCountDetails dcd WHERE dcd.dailyCount = :dailyCount AND dcd.productId = :productId AND dcd.category=:category")
	Optional<DailyCountDetails> findByDailyCountAndProductIdAndCategory(@Param("dailyCount") DailyCount dailyCount, @Param("productId") Integer productId,@Param("category") Integer category);
}
