package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = { "user" })
@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Integer productId;
  private String name;
  private String shortName;
  private BigDecimal price;
  private String status;
  private String serialPrefix;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<User> users;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<DailyCountDetails> dailyCountDetails;
}
