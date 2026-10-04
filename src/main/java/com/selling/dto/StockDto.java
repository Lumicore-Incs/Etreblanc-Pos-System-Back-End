package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class StockDto {
    private Integer stock_id;
    private String type;
    private int totalQuantity;
    private String status;

    public StockDto(Integer stock_id, String type, Integer totalQuantity, String status) {
        this.stock_id = stock_id;
        this.type = type;
        this.totalQuantity = totalQuantity;
        this.status = status;
    }
}
