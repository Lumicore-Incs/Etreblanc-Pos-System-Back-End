package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserSummeryDetailsGet {
    private LocalDateTime date;
    private Integer orderQty;
    private Integer qty;
    private BigDecimal commission;
    private BigDecimal total;
    private Double bonus;
}
