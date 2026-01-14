package com.selling.dto.get;

import com.selling.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OrderDetailsDtoGet {
    private Integer orderDetailsId;
    private Integer qty;
    private BigDecimal total;
    private ProductDto productId;
    private Integer orderId;
}