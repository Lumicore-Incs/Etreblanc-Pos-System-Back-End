package com.selling.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class CustomerRequestDTO {
  private Integer customerId;
  private String name;
  private String serialNo;
  private String address;
  private String contact01;
  private String contact02;
  private LocalDateTime date;
  private String remark;
  private String status; // active, inactive
  private Integer userId;
  private List<OrderDetailsDto> items;
  private BigDecimal totalPrice;
}
