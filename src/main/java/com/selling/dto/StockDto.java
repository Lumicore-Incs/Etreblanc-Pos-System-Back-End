package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class StockDto {
    private Integer stock_id;
    private String type;
    private Date date;
    private int totalQuantity;
    private int quantity;
    private String status;
}
