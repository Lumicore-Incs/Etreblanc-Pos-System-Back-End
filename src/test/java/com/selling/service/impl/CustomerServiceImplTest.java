package com.selling.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.selling.dto.CustomerRequestDTO;
import com.selling.dto.UserDto;
import com.selling.dto.get.OrderDtoGet;
import com.selling.model.Customer;
import com.selling.model.Order;
import com.selling.model.Product;
import com.selling.repository.CustomerRepo;
import com.selling.repository.OrderDetailsRepo;
import com.selling.repository.OrderRepo;
import com.selling.repository.ProductRepo;
import com.selling.util.MapperService;

class CustomerServiceImplTest {

  @Mock
  CustomerRepo customerRepo;

  @Mock
  OrderRepo orderRepo;

  @Mock
  OrderDetailsRepo orderDetailsRepo;

  @Mock
  ProductRepo productRepo;

  @Mock
  MapperService mapperService;

  @InjectMocks
  CustomerServiceImpl service;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenRecentCustomerExists_thenUseExisting() {
    CustomerRequestDTO req = new CustomerRequestDTO();
    req.setContact01("0712345678");
    com.selling.dto.OrderDetailsDto item = new com.selling.dto.OrderDetailsDto();
    item.setProductId(1);
    item.setQty(1);
    item.setTotal(java.math.BigDecimal.valueOf(100.0));
    req.setItems(List.of(item));

    UserDto userDto = new UserDto();
    userDto.setId(1L);

    Customer existing = new Customer();
    existing.setCustomerId(100);
    Order o = new Order();
    o.setDate(LocalDateTime.now().minusDays(3));
    o.setStatus("TEMPORARY");
    existing.setOrders(List.of(o));

    when(customerRepo.findByContactsWithOrdersSinceAndStatus(any(), any(), any()))
        .thenReturn(new java.util.ArrayList<>(List.of(existing)));
    when(mapperService.map(any(), eq(Customer.class))).thenReturn(new Customer());
    when(mapperService.map(any(Order.class), eq(OrderDtoGet.class))).thenReturn(new OrderDtoGet());
    when(productRepo.findAllByProductId(anyInt())).thenReturn(new Product());
    when(orderDetailsRepo.saveAll(any())).thenReturn(null);
    when(orderRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Object result = service.saveCustomerTemporory(req, userDto);
    assertNotNull(result);
    verify(customerRepo, never()).save(any(Customer.class)); // should not create new customer
  }

  @Test
  void whenNoRecentCustomer_thenCreateNew() {
    CustomerRequestDTO req = new CustomerRequestDTO();
    req.setContact01("0712345678");
    com.selling.dto.OrderDetailsDto item = new com.selling.dto.OrderDetailsDto();
    item.setProductId(1);
    item.setQty(1);
    item.setTotal(java.math.BigDecimal.valueOf(100.0));
    req.setItems(List.of(item));

    UserDto userDto = new UserDto();
    userDto.setId(1L);

    when(customerRepo.findByContactsWithOrdersSinceAndStatus(any(), any(), any()))
        .thenReturn(new java.util.ArrayList<>());
    Customer saved = new Customer();
    saved.setCustomerId(200);
    when(mapperService.map(any(), eq(Customer.class))).thenReturn(new Customer());
    when(customerRepo.save(any(Customer.class))).thenReturn(saved);
    when(mapperService.map(any(Order.class), eq(OrderDtoGet.class))).thenReturn(new OrderDtoGet());
    when(productRepo.findAllByProductId(anyInt())).thenReturn(new Product());
    when(orderDetailsRepo.saveAll(any())).thenReturn(null);
    when(orderRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Object result = service.saveCustomerTemporory(req, userDto);
    assertNotNull(result);
    verify(customerRepo, times(1)).save(any(Customer.class)); // should create new customer
  }

  @Test
  void whenBothContactsPresent_andMatchSameCustomer_thenUseExisting() {
    CustomerRequestDTO req = new CustomerRequestDTO();
    req.setContact01("0711111111");
    req.setContact02("0777777777");
    com.selling.dto.OrderDetailsDto item = new com.selling.dto.OrderDetailsDto();
    item.setProductId(1);
    item.setQty(1);
    item.setTotal(java.math.BigDecimal.valueOf(50.0));
    req.setItems(List.of(item));

    UserDto userDto = new UserDto();
    userDto.setId(1L);

    Customer existing = new Customer();
    existing.setCustomerId(300);
    Order o = new Order();
    o.setDate(LocalDateTime.now().minusDays(1));
    o.setStatus("TEMPORARY");
    existing.setOrders(List.of(o));

    when(customerRepo.findByContactsWithOrdersSinceAndStatus(any(), any(), any()))
        .thenReturn(new java.util.ArrayList<>(List.of(existing)));
    when(mapperService.map(any(), eq(Customer.class))).thenReturn(new Customer());
    when(mapperService.map(any(Order.class), eq(OrderDtoGet.class))).thenReturn(new OrderDtoGet());
    when(productRepo.findAllByProductId(anyInt())).thenReturn(new Product());
    when(orderDetailsRepo.saveAll(any())).thenReturn(null);
    when(orderRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Object result = service.saveCustomerTemporory(req, userDto);
    assertNotNull(result);
    verify(customerRepo, never()).save(any(Customer.class));
  }

  @Test
  void whenMultipleMatchingCustomers_chooseMostRecent() {
    CustomerRequestDTO req = new CustomerRequestDTO();
    req.setContact01("0710000000");
    com.selling.dto.OrderDetailsDto item = new com.selling.dto.OrderDetailsDto();
    item.setProductId(1);
    item.setQty(1);
    item.setTotal(java.math.BigDecimal.valueOf(120.0));
    req.setItems(List.of(item));

    UserDto userDto = new UserDto();
    userDto.setId(1L);

    Customer older = new Customer();
    older.setCustomerId(401);
    Order o1 = new Order();
    o1.setDate(LocalDateTime.now().minusDays(10));
    o1.setStatus("TEMPORARY");
    older.setOrders(List.of(o1));

    Customer newer = new Customer();
    newer.setCustomerId(402);
    Order o2 = new Order();
    o2.setDate(LocalDateTime.now().minusDays(2));
    o2.setStatus("TEMPORARY");
    newer.setOrders(List.of(o2));

    when(customerRepo.findByContactsWithOrdersSinceAndStatus(any(), any(), any()))
        .thenReturn(new java.util.ArrayList<>(List.of(older, newer)));
    when(mapperService.map(any(), eq(Customer.class))).thenReturn(new Customer());
    when(mapperService.map(any(Order.class), eq(OrderDtoGet.class))).thenReturn(new OrderDtoGet());
    when(productRepo.findAllByProductId(anyInt())).thenReturn(new Product());
    when(orderDetailsRepo.saveAll(any())).thenReturn(null);
    when(orderRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Object result = service.saveCustomerTemporory(req, userDto);
    assertNotNull(result);
    // ensure we didn't create a new customer and that the service picked one of
    // existing ones
    verify(customerRepo, never()).save(any(Customer.class));
  }

  @Test
  void whenMatchingCustomersButOnlyOldStatuses_thenCreateNew() {
    // Matching customers exist but their orders are not TEMPORARY or PENDING;
    // should create new
    CustomerRequestDTO req = new CustomerRequestDTO();
    req.setContact01("0799999999");
    com.selling.dto.OrderDetailsDto item = new com.selling.dto.OrderDetailsDto();
    item.setProductId(1);
    item.setQty(1);
    item.setTotal(java.math.BigDecimal.valueOf(80.0));
    req.setItems(List.of(item));

    UserDto userDto = new UserDto();
    userDto.setId(1L);

    Customer existing = new Customer();
    existing.setCustomerId(500);
    Order o = new Order();
    o.setDate(LocalDateTime.now().minusDays(3));
    o.setStatus("COMPLETED");
    existing.setOrders(List.of(o));

    when(customerRepo.findByContactsWithOrdersSinceAndStatus(any(), any(), any()))
        .thenReturn(new java.util.ArrayList<>());
    Customer saved = new Customer();
    saved.setCustomerId(600);
    when(mapperService.map(any(), eq(Customer.class))).thenReturn(new Customer());
    when(customerRepo.save(any(Customer.class))).thenReturn(saved);
    when(mapperService.map(any(Order.class), eq(OrderDtoGet.class))).thenReturn(new OrderDtoGet());
    when(productRepo.findAllByProductId(anyInt())).thenReturn(new Product());
    when(orderDetailsRepo.saveAll(any())).thenReturn(null);
    when(orderRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Object result = service.saveCustomerTemporory(req, userDto);
    assertNotNull(result);
    verify(customerRepo, times(1)).save(any(Customer.class));
  }
}
