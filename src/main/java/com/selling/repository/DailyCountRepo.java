package com.selling.repository;

import com.selling.model.DailyCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCountRepo extends JpaRepository<DailyCount, Integer> {
    
    @Query("SELECT dc FROM DailyCount dc WHERE dc.date = :date")
    Optional<DailyCount> findByDate(@Param("date") Date date);
    
    @Query("SELECT dc FROM DailyCount dc  ORDER BY dc.lastTime DESC")
    List<DailyCount> findByDate();
    
    @Query("SELECT dc FROM DailyCount dc WHERE dc.date BETWEEN :startDate AND :endDate ORDER BY dc.date DESC, dc.lastTime DESC")
    List<DailyCount> findByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}

