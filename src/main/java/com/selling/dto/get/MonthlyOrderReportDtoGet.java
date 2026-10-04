package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class MonthlyOrderReportDtoGet {
    private Date date;
    private int totalOrders;
    private int totalItems;
}
