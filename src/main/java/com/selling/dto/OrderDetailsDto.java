package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OrderDetailsDto {
  private Integer orderDetailsId;
  private Integer qty;
  private BigDecimal total;
  private Integer productId;
  private Integer orderId;
}
