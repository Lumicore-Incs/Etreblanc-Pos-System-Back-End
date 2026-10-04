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
public class StockDetailsDto {
    private Integer id;
    private int quantity;
    private Date date;
    private String status;
    private String type;
    private String stock_id;
}
