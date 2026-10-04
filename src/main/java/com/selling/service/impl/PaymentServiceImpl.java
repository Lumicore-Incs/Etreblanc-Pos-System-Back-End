package com.selling.service.impl;

import com.selling.dto.PaymentDTO;
import com.selling.dto.PaymentDetailsDTO;
import com.selling.model.Payment;
import com.selling.model.PaymentDetails;
import com.selling.model.User;
import com.selling.repository.PaymentDetailsRepo;
import com.selling.repository.PaymentRepo;
import com.selling.repository.UserRepo;
import com.selling.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final PaymentDetailsRepo paymentDetailsRepo;

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        User user = userRepo.findById(paymentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + paymentDTO.getUserId()));

        Payment payment = modelMapper.map(paymentDTO, Payment.class);
        payment.setUser(user);

        // Map PaymentDetails and set parent Payment
        if (paymentDTO.getPaymentDetails() != null) {
            List<PaymentDetails> details = paymentDTO.getPaymentDetails().stream()
                    .map(d -> {
                        PaymentDetails detail = modelMapper.map(d, PaymentDetails.class);
                        detail.setPayment(payment);
                        return detail;
                    }).collect(Collectors.toList());
            payment.setPaymentDetails(details);
        }

        Payment savedPayment = paymentRepo.save(payment);
        return mapToDTO(savedPayment);
    }

    @Override
    public PaymentDTO getPaymentByUserId(Long userId) {
        try {
            Optional<Payment> byUserId = paymentRepo.findByUserId(userId);
            if (byUserId.isPresent()) {
                Payment payment = byUserId.get();
                return mapToDTO(payment);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving payment: " + e.getMessage());
        }
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        try {
            return paymentRepo.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving payments: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment existingPayment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        existingPayment.setCommission(paymentDTO.getCommission());
        existingPayment.setBasicSalary(paymentDTO.getBasicSalary());

        if (paymentDTO.getUserId() != null) {
            User user = userRepo.findById(paymentDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + paymentDTO.getUserId()));
            existingPayment.setUser(user);
        }

        // Simplistic update for details: clear and add new ones (or you could do a more complex merge)
        if (paymentDTO.getPaymentDetails() != null) {
            existingPayment.getPaymentDetails().clear();
            List<PaymentDetails> newDetails = paymentDTO.getPaymentDetails().stream()
                    .map(d -> {
                        PaymentDetails detail = modelMapper.map(d, PaymentDetails.class);
                        detail.setPayment(existingPayment);
                        return detail;
                    }).collect(Collectors.toList());
            existingPayment.getPaymentDetails().addAll(newDetails);
        }

        Payment updatedPayment = paymentRepo.save(existingPayment);
        return mapToDTO(updatedPayment);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepo.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepo.deleteById(id);
    }

    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = modelMapper.map(payment, PaymentDTO.class);
        if (payment.getUser() != null) {
            dto.setUserId(payment.getUser().getId());
        }
        if (payment.getPaymentDetails() != null) {
            dto.setPaymentDetails(payment.getPaymentDetails().stream()
                    .map(d -> {
                        PaymentDetailsDTO detailDTO = modelMapper.map(d, PaymentDetailsDTO.class);
                        detailDTO.setPaymentId(payment.getId());
                        return detailDTO;
                    }).collect(Collectors.toList()));
        }
        return dto;
    }


    //==============payment details ==================
    @Override
    public PaymentDetailsDTO createPaymentDetails(PaymentDetailsDTO paymentDTO) {
        PaymentDetails map = modelMapper.map(paymentDTO, PaymentDetails.class);
        Optional<Payment> byId = paymentRepo.findById(paymentDTO.getPaymentId());
        byId.ifPresent(map::setPayment);
        PaymentDetails savedPayment = paymentDetailsRepo.save(map);
        return modelMapper.map(savedPayment, PaymentDetailsDTO.class);
    }

    @Override
    @Transactional
    public void deletePaymentDetails(Long id) {
        if (!paymentDetailsRepo.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentDetailsRepo.deleteById(id);
    }

    @Override
    public PaymentDetailsDTO updatePaymentDetails(Long id, PaymentDetailsDTO paymentDTO) {
        PaymentDetails existingPayment = paymentDetailsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        existingPayment.setDate(paymentDTO.getDate());
        existingPayment.setMonthlyQty(paymentDTO.getMonthlyQty());
        existingPayment.setTotalCommission(paymentDTO.getTotalCommission());
        existingPayment.setStatus(paymentDTO.getStatus());
        existingPayment.setTotalAmount(paymentDTO.getTotalAmount());

        PaymentDetails updatedPayment = paymentDetailsRepo.save(existingPayment);
        return modelMapper.map(updatedPayment, PaymentDetailsDTO.class);
    }
}
