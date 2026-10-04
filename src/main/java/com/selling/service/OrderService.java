package com.selling.service;

import com.selling.dto.CustomerRequestDTO;
import com.selling.dto.PaginationResponse;
import com.selling.dto.TrackingDto;
import com.selling.dto.UserDto;
import com.selling.dto.get.MonthlyOrderReportDtoGet;
import com.selling.dto.get.OrderDtoGet;
import com.selling.model.Product;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public interface OrderService {
  List<OrderDtoGet> getAllTodayOrder();

  List<OrderDtoGet> getAllTodayOrderByUserId(UserDto userDto);

  List<OrderDtoGet> getAllOrder();

  List<MonthlyOrderReportDtoGet> getAllOrderByUserId(long userDto, LocalDate date, YearMonth month);

  PaginationResponse<OrderDtoGet> getAllTodayOrderPaginated(int page, int size, String search, String status,
      Integer productId);

  PaginationResponse<OrderDtoGet> getAllTodayOrderByUserIdPaginated(UserDto userDto, int page, int size, String search,
      String status, Integer productId);

  PaginationResponse<OrderDtoGet> getAllOrderPaginated(int page, int size, String search, String status,
                                                       Integer productId, Date date);

  PaginationResponse<OrderDtoGet> getAllOrderByUserIdPaginated(UserDto userDto, int page, int size, String search,
                                                               String status, Integer productId, Date date);

  List<OrderDtoGet> getTemporaryOrders(UserDto dto);

  void updateOrderDetails(UserDto userDto);

  Object resolveDuplicateOrder(Integer orderId, String userRole, CustomerRequestDTO requestDTO);

  Object deleteOrder(Integer orderId);

  // Generate serial number for an order based on product
  String generateOrderSerialNumber(Product product, UserDto userDto);

  String trackingUpload(List<TrackingDto> trackingList);

  ArrayList<String> getUrgentOrders(Long id, String date);

}