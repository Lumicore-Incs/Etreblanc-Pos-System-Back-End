package com.selling.repository;

import com.selling.model.Order;
import com.selling.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepo extends JpaRepository<OrderDetails, Integer> {
    List<OrderDetails> findByOrder(Order orderId);
}
