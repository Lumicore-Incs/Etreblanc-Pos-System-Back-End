package com.selling.controller;

import com.selling.dto.ApiResponse;
import com.selling.dto.PaymentDTO;
import com.selling.dto.PaymentDetailsDTO;
import com.selling.dto.UserDto;
import com.selling.dto.get.UserAnalyticsDtoGet;
import com.selling.service.DashBoardService;
import com.selling.service.PaymentService;
import com.selling.service.UserService;
import com.selling.util.JWTTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin()
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    @Autowired
    private JWTTokenGenerator jwtTokenGenerator;
    @Autowired
    private final PaymentService paymentService;
    @Autowired
    private final DashBoardService dashBoardService;
    @Autowired
    private UserService userService;



    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @RequestHeader(name = "Authorization") String authorizationHeader, @RequestBody PaymentDTO paymentDTO) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
            PaymentDTO created = paymentService.createPayment(paymentDTO);
            return new ResponseEntity<>(ApiResponse.created(created), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("userId") Long userId) {
         try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
              PaymentDTO payment = paymentService.getPaymentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
      
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments(@RequestHeader(name = "Authorization") String authorizationHeader) {
         try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePayment(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("id") Long id,
            @RequestBody PaymentDTO paymentDTO) {
                 try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
            PaymentDTO updated = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", updated));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("id") Long id) {
         try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
             paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //==============payment details ==================

    @PostMapping("/details")
    public ResponseEntity<ApiResponse<PaymentDetailsDTO>> createPaymentDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader, @RequestBody PaymentDetailsDTO paymentDetailsDTO) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
            PaymentDetailsDTO created = paymentService.createPaymentDetails(paymentDetailsDTO);
            return new ResponseEntity<>(ApiResponse.created(created), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/details/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentDetails(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("id") Long id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
            paymentService.deletePaymentDetails(id);
            return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/Details/{id}")
    public ResponseEntity<ApiResponse<PaymentDetailsDTO>> updatePaymentDetails(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("id") Long id,
                                                                 @RequestBody PaymentDetailsDTO dto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
            PaymentDetailsDTO updated = paymentService.updatePaymentDetails(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", updated));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    //==============payment details ==================

    @GetMapping("/qty/{id}")
    public ResponseEntity<ApiResponse<UserAnalyticsDtoGet>> getAllDetails(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("id") Long id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token", 401));
            }
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Error retrieving products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        UserDto userDto = userService.getUserById(String.valueOf(id));
        UserAnalyticsDtoGet userAnalyticsDtoGet = new UserAnalyticsDtoGet();
        if(userDto.getRole().equals("SUPER USER") || userDto.getRole().equals("ADMIN")){
            userAnalyticsDtoGet.setTodayQty(0); 
            userAnalyticsDtoGet.setMonthQty(0);
            userAnalyticsDtoGet.setDeliveredQty(0);
            userAnalyticsDtoGet.setReturnQty(0);
        }
        userAnalyticsDtoGet.setTodayQty(dashBoardService.getTodayOrder(userDto));
        userAnalyticsDtoGet.setMonthQty(dashBoardService.getTotalOrder(userDto));
        userAnalyticsDtoGet.setDeliveredQty(dashBoardService.getConformOrderByUser(userDto));
        userAnalyticsDtoGet.setReturnQty(dashBoardService.getCancelOrderByUser(userDto));
//        userAnalyticsDtoGet.setTotalSale();
        return ResponseEntity.ok(ApiResponse.success(userAnalyticsDtoGet));
    }
}
