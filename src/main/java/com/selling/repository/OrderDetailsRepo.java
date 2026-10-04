package com.selling.repository;

import com.selling.model.Order;
import com.selling.model.OrderDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailsRepo extends JpaRepository<OrderDetails, Integer> {
    List<OrderDetails> findByOrder(Order orderId);

    @Transactional
    @Modifying
    @Query("DELETE FROM OrderDetails od WHERE od.order = :order")
    void deleteAllByOrder(@Param("order") Order order);
}
