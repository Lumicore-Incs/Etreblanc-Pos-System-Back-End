package com.selling.repository;

import com.selling.model.Stock;
import com.selling.model.StockDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StockDetailsRepo extends JpaRepository<StockDetails, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM StockDetails sd WHERE sd.stock = :stock")
    void deleteAllByStock(@Param("stock") Stock stock);

    @Query("SELECT sd FROM StockDetails sd WHERE " +
           "(:type IS NULL OR sd.type = :type) AND " +
           "(:status IS NULL OR sd.status = :status) AND " +
           "(cast(:date as date) IS NULL OR sd.date = :date) AND " +
           "(cast(:startDate as date) IS NULL OR sd.date >= :startDate) " +
           "ORDER BY sd.id DESC")
    List<StockDetails> filterStockDetails(
            @Param("type") String type,
            @Param("status") String status,
            @Param("date") java.sql.Date date,
            @Param("startDate") java.sql.Date startDate);
}
