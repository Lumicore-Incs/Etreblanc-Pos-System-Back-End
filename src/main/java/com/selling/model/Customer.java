package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "customer")
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "customer_id")
  private Integer customerId;

  private String name;
  private String customerName;

  private String address;

  private String contact01;
  private String contact02;

  private LocalDateTime date;

  private String status; // active, inactive

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  private User user;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
  private List<Order> orders;
}
