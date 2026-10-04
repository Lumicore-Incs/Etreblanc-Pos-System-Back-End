package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class DailyCountDetailsDtoGet {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer category;
    private Integer qty;
}
