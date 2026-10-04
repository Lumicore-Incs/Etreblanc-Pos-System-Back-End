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
public class ProductDto {
  private Long productId;
  private String name;
  private String shortName;
  private BigDecimal price;
  private String status;
  private String serialPrefix;
}
