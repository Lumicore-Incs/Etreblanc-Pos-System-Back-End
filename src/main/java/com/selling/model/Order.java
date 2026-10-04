package com.selling.model;

import jakarta.persistence.*;
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
@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Integer orderId;

  private String serialNo;

  private String weyBillId;

  private BigDecimal totalPrice;

  private String OrderType;

  private LocalDateTime date;

  @Column(name = "delivery_date")
  private LocalDateTime deliveryDate;

  private String trackingId;

  private String status;

  private String remark;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  private User user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderDetails> orderDetails;
}
