package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long id;
    private Double commission;
    private Double basicSalary;
    private Long userId;
    private List<PaymentDetailsDTO> paymentDetails;
}
