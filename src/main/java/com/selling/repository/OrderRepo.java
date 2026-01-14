package com.selling.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.selling.model.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.selling.model.Order;
import com.selling.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepo extends JpaRepository<Order, Integer> {
  @EntityGraph(attributePaths = { "customer", "orderDetails", "orderDetails.product" })
  List<Order> findByUser(User userId);

  List<Order> findAllByOrderByOrderIdDesc();

  List<Order> findTop200ByOrderByOrderIdDesc();

  // Get last saved order (most recent) to determine global serial numeric part
  Order findTopBySerialNoStartingWithOrderBySerialNoDesc(String prefix);


  int countByStatusAndDateBetween(String deliver, LocalDateTime startOfMonth, LocalDateTime now);

  int countByCustomerUserEmailAndStatusAndDateBetween(String email, String deliver, LocalDateTime startOfMonth,
      LocalDateTime now);

  // Find orders by status
  List<Order> findByStatus(String status);

  // Find orders between two datetimes (useful for today's orders)
  List<Order> findByDateBetween(LocalDateTime start, LocalDateTime end);

  List<Order> findByDateBetweenAndUser_Id(LocalDateTime start, LocalDateTime end, Long userId);

  int countByUserId(Long id);

  // serialNo මගින් Order සොයාගැනීම
  Optional<Order> findBySerialNo(String serialNo);

    Order findByCustomer(Customer customer);
}
