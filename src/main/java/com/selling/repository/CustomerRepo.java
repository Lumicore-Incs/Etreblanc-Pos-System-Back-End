package com.selling.repository;

import com.selling.dto.get.ExcelTypeDto;
import com.selling.model.Customer;
import com.selling.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
  List<Customer> findAllByUserId(Long id);

  @Query("SELECT new com.selling.dto.get.ExcelTypeDto(" +
      "o.serialNo, c.name, c.address, c.contact01, c.contact02,null,o.remark) " +
      "FROM Customer c " +
      "JOIN c.orders o " +
      "JOIN o.orderDetails od " +
      "WHERE c.status = 'PENDING'" +
      "ORDER BY od.qty asc ")
  List<ExcelTypeDto> findPendingOrdersWithQuantities();

  @Query("SELECT o " +
      "FROM Customer c " +
      "JOIN c.orders o " +
      "JOIN o.orderDetails od " +
      "WHERE o.status = 'PENDING'" +
       "AND (od.product.productId = :productId) " +
      "ORDER BY od.qty ASC")
  List<Order> findPendingOrdersWithQuantities(@Param("productId") Integer productId);

  @Query("SELECT o " +
          "FROM Customer c " +
          "JOIN c.orders o " +
          "JOIN o.orderDetails od " +
          "WHERE c.status = 'PENDING'" +
          "AND o.status = 'PENDING'"+
          "ORDER BY od.qty ASC")
  List<Order> findAllPendingOrdersWithQuantities();

  @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o "
      + "WHERE (c.contact01 IN :contacts OR c.contact02 IN :contacts) "
      + "AND o.date >= :since "
      + "AND o.status IN :statuses")
  List<Customer> findByContactsWithOrdersSinceAndStatus(@Param("contacts") Collection<String> contacts,
      @Param("since") java.time.LocalDateTime since, @Param("statuses") List<String> statuses);
}
