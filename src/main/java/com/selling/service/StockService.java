package com.selling.service;


import com.selling.dto.StockDto;
import com.selling.model.OrderDetails;

import java.util.List;

public interface StockService {
    StockDto saveStock(StockDto stockDto);

    StockDto getStockById(Long aLong);

    List<StockDto> getAllStock();

    StockDto updateStock(Integer id, StockDto stockDto);

    boolean deleteStock(Integer id);

    List<StockDto> getAllStockByType(String name);

    void updateStockByName(String name, Integer qty);

    void updateStockQty(List<OrderDetails> details);
}
