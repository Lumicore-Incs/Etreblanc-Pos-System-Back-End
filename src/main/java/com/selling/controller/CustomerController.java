package com.selling.controller;

import com.selling.dto.ApiResponse;
import com.selling.dto.CustomerRequestDTO;
import com.selling.dto.UserDto;
import com.selling.dto.get.CustomerDtoGet;
import com.selling.service.CustomerService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {
  @Autowired
  private JWTTokenGenerator jwtTokenGenerator;

  @Autowired
  private final CustomerService customerService;

  @PostMapping
  public ResponseEntity<ApiResponse<Object>> saveCustomer(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @RequestBody @Valid CustomerRequestDTO requestDTO) {
    if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Invalid token", 401));
    }
    UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
    Object savedCustomer = customerService.saveCustomerTemporory(requestDTO, userDto);
    if (savedCustomer.equals("true")) {
      return ResponseEntity.status(HttpStatus.MULTI_STATUS)
          .body(ApiResponse.success("Customer reused", savedCustomer));
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(savedCustomer));
  }

  @GetMapping()
  public ResponseEntity<Object> getAllCustomer(@RequestHeader(name = "Authorization") String authorizationHeader) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
      if (Objects.equals(userDto.getRole(), "SUPER USER") || Objects.equals(userDto.getRole(), "ADMIN")) {
        List<CustomerDtoGet> allCustomer = customerService.getAllCustomer();
        return new ResponseEntity<>(allCustomer, HttpStatus.OK);
      } else {

        List<CustomerDtoGet> allCustomer = customerService.getAllCustomerByUserId(userDto);
        return new ResponseEntity<>(allCustomer, HttpStatus.OK);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteCustomer(@RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable("id") Integer id) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      boolean isDeleted = customerService.deleteCustomer(id);
      if (isDeleted) {
        return new ResponseEntity<>("Customer disabled successfully", HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Object> updateCustomer(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable("id") Integer id,
      @RequestBody CustomerRequestDTO requestDTO) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      Object updatedCustomer = customerService.updateCustomer(id, requestDTO);
      if (updatedCustomer != null) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Customer updated", updatedCustomer));
      } else {
        return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error updating customer: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
