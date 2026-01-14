package com.selling.repository;

import com.selling.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepo extends JpaRepository<Stock, Integer> {
    List<Stock> findAllByType(String name);

    @Query("SELECT s FROM Stock s WHERE s.type = :type ORDER BY s.stock_id DESC LIMIT 1")
    Stock findTopByType(@Param("type") String type);

    List<Stock> findAllByOrderByDateDesc();


}
