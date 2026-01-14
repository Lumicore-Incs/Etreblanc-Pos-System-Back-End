package com.selling.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.selling.repository.*;
import com.selling.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.selling.dto.CustomerRequestDTO;
import com.selling.dto.UserDto;
import com.selling.dto.get.CustomerDtoGet;
import com.selling.dto.get.OrderDtoGet;
import com.selling.model.Customer;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.model.Product;
import com.selling.model.User;
import com.selling.service.CustomerService;
import com.selling.service.OrderService;
import com.selling.util.MapperService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepo customerRepository;
  private final OrderRepo orderRepository;
  private final OrderDetailsRepo orderDetailsRepository;
  private final ProductRepo productRepository;
  private final MapperService mapperService;
  private final OrderService orderService;
  private final StockService stockService;

  @Override
  @Transactional
  public Object saveCustomerTemporory(CustomerRequestDTO requestDTO, UserDto userDto) {
    // collect both contacts if provided (contact01 and contact02)
    String c1 = requestDTO == null ? null : requestDTO.getContact01();
    String c2 = requestDTO == null ? null : requestDTO.getContact02();
    List<String> contacts = new ArrayList<>();
    if (c1 != null && !c1.isBlank())
      contacts.add(c1.trim());
    if (c2 != null && !c2.isBlank() && !contacts.contains(c2.trim()))
      contacts.add(c2.trim());

    Optional<Customer> opt = Optional.empty();
    if (!contacts.isEmpty()) {
      LocalDateTime since = LocalDateTime.now().minusWeeks(2);
      List<String> statuses = List.of("TEMPORARY", "PENDING");
      List<Customer> recent = customerRepository.findByContactsWithOrdersSinceAndStatus(contacts, since, statuses);
      if (recent != null && !recent.isEmpty()) {
        // copy into mutable list then pick the customer with the most recent order
        List<Customer> mutable = new ArrayList<>(recent);
        mutable.sort((a, b) -> {
          LocalDateTime ma = a.getOrders().stream().map(Order::getDate).max(LocalDateTime::compareTo)
              .orElse(LocalDateTime.MIN);
          LocalDateTime mb = b.getOrders().stream().map(Order::getDate).max(LocalDateTime::compareTo)
              .orElse(LocalDateTime.MIN);
          return mb.compareTo(ma);
        });
        opt = Optional.of(mutable.get(0));
        requestDTO.setCustomerId(opt.get().getCustomerId());
      }
    }

    if (opt.isEmpty()) {
      // new customer
      Customer newCustomer = createNewCustomer(requestDTO, userDto);
      opt = Optional.of(newCustomer);
    }
    return createNewOrder(requestDTO, opt, userDto);
  }

  private Customer createNewCustomer(CustomerRequestDTO requestDTO, UserDto userDto) {
    Customer newCustomer = mapperService.map(requestDTO, Customer.class);
    if (newCustomer.getUser() == null) {
      newCustomer.setUser(mapperService.map(userDto, User.class));
    }
    return customerRepository.save(newCustomer);
  }

  // 2. Create and Save Order
  private Object createNewOrder(CustomerRequestDTO requestDTO, Optional<Customer> opt, UserDto userDto) {
    Order order = new Order();
    order.setCustomer(opt.get());
    if (userDto != null) {
      order.setUser(mapperService.map(userDto, User.class));
    }
    order.setDate(LocalDateTime.now());
    if (requestDTO.getCustomerId() == null) {
      order.setStatus("PENDING");
    } else {
      order.setStatus("TEMPORARY");
    }
    order.setRemark(requestDTO.getRemark());
    order.setTrackingId(generateTrackingId());
    order.setTotalPrice(requestDTO.getTotalPrice());

    // generate serial based on product of first item (global increment)
    Product firstProduct = null;
    if (requestDTO.getItems() != null && !requestDTO.getItems().isEmpty()) {
      firstProduct = productRepository.findAllByProductId(requestDTO.getItems().get(0).getProductId());
    }
    String serial = orderService.generateOrderSerialNumber(firstProduct);
    order.setSerialNo(serial);

    Order savedOrder = orderRepository.save(order);

    // 3. Save Order Details
    List<OrderDetails> orderDetailsList = requestDTO.getItems().stream()
        .map(item -> {
          Product product = productRepository.findAllByProductId(item.getProductId());
          if (product == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Product not found with id: " + item.getProductId());
          }

          OrderDetails orderDetails = new OrderDetails();
          orderDetails.setOrder(savedOrder);
          orderDetails.setProduct(product);
          orderDetails.setQty(item.getQty());
          orderDetails.setTotal(item.getTotal());

          stockService.updateStockByName(product.getName(), item.getQty());

          return orderDetails;
        })
        .collect(Collectors.toList());

    orderDetailsRepository.saveAll(orderDetailsList);

    savedOrder.setOrderDetails(orderDetailsList);
    // 4. Prepare and Return Response - Return DTO instead of entity to avoid
    // circular references
    OrderDtoGet orderDtoGet = mapperService.map(savedOrder, OrderDtoGet.class);
    return orderDtoGet;
  }

  @Override
  public List<CustomerDtoGet> getAllCustomer() {
    List<CustomerDtoGet> customerDtoGetList = new ArrayList<>();
    List<Customer> allCustomer = customerRepository.findAll();
    for (Customer customer : allCustomer) {
      CustomerDtoGet dto = mapperService.map(customer, CustomerDtoGet.class);
      if (customer.getUser() != null) {
        dto.setUser(mapperService.map(customer.getUser(), com.selling.dto.UserDto.class));
      }
      customerDtoGetList.add(dto);
    }
    return customerDtoGetList;
  }

  @Override
  public List<CustomerDtoGet> getAllCustomerByUserId(UserDto userDto) {
    List<CustomerDtoGet> customerDtoGetList = new ArrayList<>();
    List<Customer> allCustomer = customerRepository.findAllByUserId(Long.valueOf(userDto.getId()));
    for (Customer customer : allCustomer) {
      CustomerDtoGet dto = mapperService.map(customer, CustomerDtoGet.class);
      if (customer.getUser() != null) {
        dto.setUser(mapperService.map(customer.getUser(), com.selling.dto.UserDto.class));
      }
      customerDtoGetList.add(dto);
    }
    return customerDtoGetList;
  }

  @Transactional
  @Override
  public boolean deleteCustomer(Integer id) {
    Optional<Customer> customerOptional = customerRepository.findById(id);
    if (customerOptional.isPresent()) {
      Customer customer = customerOptional.get();
      List<Order> orders = customer.getOrders();
      Order lastOrder = orders.get(orders.size() - 1);
      List<OrderDetails> orderDetails = lastOrder.getOrderDetails();
      orderRepository.deleteById(lastOrder.getOrderId());
      return true;
    }else {
      return false;
    }
  }

  private String generateTrackingId() {
    return "TRK" + System.currentTimeMillis();
  }

  @Override
  public Object updateCustomer(Integer id, CustomerRequestDTO requestDTO) {
    try {
      Optional<Customer> customerOptional = customerRepository.findById(id);
      if (customerOptional.isPresent()) {
        Customer customer = customerOptional.get();

        // Update simple customer fields from requestDTO
        if (requestDTO.getName() != null)
          customer.setName(requestDTO.getName());
        if (requestDTO.getAddress() != null)
          customer.setAddress(requestDTO.getAddress());
        if (requestDTO.getContact01() != null)
          customer.setContact01(requestDTO.getContact01());
        if (requestDTO.getContact02() != null)
          customer.setContact02(requestDTO.getContact02());
        customer.setStatus(requestDTO.getStatus());

        Customer saved = customerRepository.save(customer);
        return mapperService.map(saved, CustomerDtoGet.class);
      } else {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating customer: " + e.getMessage());
    }
  }
}
