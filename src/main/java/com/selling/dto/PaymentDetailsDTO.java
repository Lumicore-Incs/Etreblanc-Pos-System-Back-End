package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsDTO {
    private Long id;
    private LocalDate date;
    private Integer monthlyQty;
    private Double totalCommission;
    private Double totalAmount;
    private String status;
    private Long paymentId;
}
