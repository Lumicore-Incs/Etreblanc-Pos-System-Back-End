package com.selling.repository;

import com.selling.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUserId(Long userId);

}
