package com.selling.dto.get;

import com.selling.dto.CustomerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OrderDtoGet {
  private Integer orderId;
  private String serialNo;
  private String weyBillId;
  private BigDecimal totalPrice;
  private String OrderType;
  private LocalDateTime date;
  private String trackingId;
  private String status;
  private String remark;
  private CustomerDto customer;
  private List<OrderDetailsDtoGet> orderDetails;
}
