package com.selling.repository;

import com.selling.dto.StockDto;
import com.selling.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepo extends JpaRepository<Stock, Integer> {
    List<Stock> findAllByType(String name);

    @Query("SELECT s FROM Stock s WHERE s.type = :type ORDER BY s.stock_id DESC LIMIT 1")
    Stock findTopByType(@Param("type") String type);

    @Query("SELECT s FROM Stock s WHERE s.type = :type AND (s.status IS NULL OR s.status <> 'DAMAGE') ORDER BY s.stock_id DESC LIMIT 1")
    Stock findTopByTypeAndStatusNotDamage(@Param("type") String type);

    @Query("SELECT s FROM Stock s WHERE s.type = :type AND s.status = :status ORDER BY s.stock_id DESC LIMIT 1")
    Stock findTopByTypeAndStatus(@Param("type") String type, @Param("status") String status);

    @Query("SELECT s FROM Stock s ORDER BY s.stock_id DESC")
    List<Stock> findAllByOrderByIdDesc();


    @Query("""
    SELECT new com.selling.dto.StockDto(
        s.stock_id,
        s.type,
        s.totalQuantity,
        s.status
    )
    FROM Stock s
""")
    List<StockDto> getStockQty();
}
