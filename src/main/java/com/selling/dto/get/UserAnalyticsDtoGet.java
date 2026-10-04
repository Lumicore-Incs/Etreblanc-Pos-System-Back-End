package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserAnalyticsDtoGet {
    private int todayQty;
    private int monthQty;
    private int deliveredQty;
    private int returnQty;
    private int totalSale;
}
