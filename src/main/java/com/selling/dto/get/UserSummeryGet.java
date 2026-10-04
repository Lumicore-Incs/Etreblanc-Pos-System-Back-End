package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserSummeryGet {
    private Integer totalOrders;
    private Integer totalItem;
    private Integer deleverd;
    private BigDecimal total;
    private List<UserSummeryDetailsGet> summery;
}
