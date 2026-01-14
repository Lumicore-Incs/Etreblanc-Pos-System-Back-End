package com.selling.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
