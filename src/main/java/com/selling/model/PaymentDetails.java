package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payment_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Integer monthlyQty;

    private Double totalCommission;

    private Double bonus;

    private Double totalAmount;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
