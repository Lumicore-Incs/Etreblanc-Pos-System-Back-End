package com.selling.service;

import java.util.List;

import com.selling.dto.CustomerRequestDTO;
import com.selling.dto.UserDto;
import com.selling.dto.get.CustomerDtoGet;

public interface CustomerService {
  Object saveCustomerTemporory(CustomerRequestDTO requestDTO, UserDto userDto);

  List<CustomerDtoGet> getAllCustomer();

  List<CustomerDtoGet> getAllCustomerByUserId(UserDto userDto);

  boolean deleteCustomer(Integer id);

  Object updateCustomer(Integer id, CustomerRequestDTO requestDTO);
}
