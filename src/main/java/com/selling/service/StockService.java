package com.selling.service;


import com.selling.dto.StockDetailsDto;
import com.selling.dto.StockDto;

import java.sql.Date;
import java.util.List;

public interface StockService {
    StockDto saveStock(StockDetailsDto stockDto);

    StockDto updateStock(Integer id, StockDetailsDto stockDto);

    boolean deleteStock(Integer id);

    void updateStockByName(String name, Integer qty);

    List<StockDetailsDto> getStockDetails(String type, String status, Date date, String month);

    List<StockDto> getStockQty();
}
