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
public class ProductDto {
  private Long productId;
  private String name;
  private String shortName;
  private BigDecimal price;
  private String status;
  private String serialPrefix;
}
