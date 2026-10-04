package com.selling.controller;

import com.selling.dto.*;
import com.selling.dto.get.MonthlyOrderReportDtoGet;
import com.selling.dto.get.OrderDtoGet;
import com.selling.service.OrderService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private JWTTokenGenerator jwtTokenGenerator;

    @Autowired
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Object> getAllTodayCustomer(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "productId", required = false) Integer productId) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
            if (Objects.equals(userDto.getRole(), "SUPER USER") || Objects.equals(userDto.getRole(), "ADMIN")) {
                PaginationResponse<OrderDtoGet> response = orderService.getAllTodayOrderPaginated(page, size, search, status,
                        productId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                PaginationResponse<OrderDtoGet> response = orderService.getAllTodayOrderByUserIdPaginated(userDto, page, size,
                        search, status, productId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/duplicate")
    public ResponseEntity<Object> resolveDuplicateOrder(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id, @RequestBody @Valid CustomerRequestDTO requestDTO) {
        try {
            System.out.println("pkl");
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }
            UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
            Object result = orderService.resolveDuplicateOrder(id, userDto.getRole(), requestDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ResponseStatusException rse) {
            return new ResponseEntity<>(rse.getReason(), rse.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Error resolving order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allCustomer")
    public ResponseEntity<Object> getAllCustomer(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "productId", required = false) Integer productId,
            @RequestParam(name = "date", required = false) Date date) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
            if (Objects.equals(userDto.getRole(), "SUPER USER") || Objects.equals(userDto.getRole(), "ADMIN")) {
                PaginationResponse<OrderDtoGet> response = orderService.getAllOrderPaginated(page, size, search, status,
                        productId, (java.sql.Date) date);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                PaginationResponse<OrderDtoGet> response = orderService.getAllOrderByUserIdPaginated(userDto, page, size,
                        search, status, productId, (java.sql.Date) date);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllCustomerByUser")
    public ResponseEntity<Object> getAllCustomerByUser(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "id", defaultValue = "0") long userId,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @RequestParam(required = false)
            LocalDate date,

            @DateTimeFormat(pattern = "yyyy-MM")
            @RequestParam(required = false)
            YearMonth month
    ){
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            List<MonthlyOrderReportDtoGet> response = orderService.getAllOrderByUserId(userId, date, month);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/duplicate")
    public ResponseEntity<Object> getDuplicateCustomerOrders(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }
            UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
            List<OrderDtoGet> temporaryOrders = orderService.getTemporaryOrders(userDto);

            return new ResponseEntity<>(temporaryOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOrder(@RequestHeader(name = "Authorization") String authorizationHeader,
                                              @PathVariable("id") Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            Object result = orderService.deleteOrder(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ResponseStatusException rse) {
            return new ResponseEntity<>(rse.getReason(), rse.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Object> trackingUpload(@RequestHeader(name = "Authorization") String authorizationHeader,
                                                 @RequestBody List<TrackingDto> trackingList) {
        try {

            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            String result = orderService.trackingUpload(trackingList);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getUrgentOrders")
    public ResponseEntity<Object> getUrgentOrders(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam("id") Long id,
            @RequestParam("date") String date) {

        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }

            ArrayList<String> orders = orderService.getUrgentOrders(id, date);
            return new ResponseEntity<>(orders, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
