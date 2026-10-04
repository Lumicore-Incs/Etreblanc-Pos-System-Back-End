package com.selling.service;

import com.selling.dto.PaymentDTO;
import com.selling.dto.PaymentDetailsDTO;

import java.util.List;

public interface PaymentService {
    PaymentDTO createPayment(PaymentDTO paymentDTO);
    PaymentDTO getPaymentByUserId(Long id);
    List<PaymentDTO> getAllPayments();
    PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO);
    void deletePayment(Long id);

    PaymentDetailsDTO createPaymentDetails(PaymentDetailsDTO paymentDetailsDTO);
    void deletePaymentDetails(Long id);
    PaymentDetailsDTO updatePaymentDetails(Long id, PaymentDetailsDTO paymentDTO);
}
