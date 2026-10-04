package com.selling.controller;

import com.selling.dto.UserDto;
import com.selling.dto.get.ExcelTypeDto;
import com.selling.dto.get.WeeklyUserOrderDto;
import com.selling.service.DashBoardService;
import com.selling.service.OrderService;
import com.selling.util.ExcelExportService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

  @Autowired
  private ExcelExportService excelExportService;
  private final OrderService orderService;
  @Autowired
  private final DashBoardService dashBoardService;
  private final JWTTokenGenerator jwtTokenGenerator;

  public void updateOrderDetails() {
//    orderService.updateOrderDetails();
  }

  @GetMapping("/updateTrackingStatus")
  public ResponseEntity<Object> updateTrackingStatus(@RequestHeader(name = "Authorization") String authorizationHeader) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
      // Execute in background
      updateOrderDetailsAsync(userDto);
      return new ResponseEntity<>("Tracking status update initiated in background", HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Error initiating tracking status update: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Async
   void updateOrderDetailsAsync(UserDto userDto) {
    try {
      orderService.updateOrderDetails(userDto);
    } catch (Exception e) {
      System.err.println("Error updating tracking status in background: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @GetMapping("/excel/{name}")
  public ResponseEntity<Object> exportToExcel(@RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable("name") String name) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      List<ExcelTypeDto> entities = dashBoardService.findOrder(name);

      ByteArrayInputStream in = excelExportService.exportToExcel(entities);

      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=data.xlsx");

      return ResponseEntity
          .ok()
          .headers(headers)
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(new InputStreamResource(in));
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/conform")
  public ResponseEntity<Object> ConformExport(
          @RequestHeader(name = "Authorization") String authorizationHeader,
          @RequestBody List<String> serialNumbers) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }

        ConformOrderAsync(serialNumbers);
      return new ResponseEntity<>("success", HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

    @Async
    void ConformOrderAsync(List<String> serialNumbers) {
        try {
            dashBoardService.ConformOrder(serialNumbers);
        } catch (Exception e) {
            System.err.println("Error updating tracking status in background: " + e.getMessage());
            e.printStackTrace();
        }
    }

  @GetMapping()
  public ResponseEntity<Object> getAllDetails(@RequestHeader(name = "Authorization") String authorizationHeader) {
    try {
      if (this.jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        Map<String, Integer> response = new HashMap<>();
        UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
        int totalOrder = dashBoardService.getTotalOrder(userDto);
        int todayOrder = dashBoardService.getTodayOrder(userDto);
        int processingOrders = dashBoardService.processingOrders(userDto);
        int conformOrder = dashBoardService.getConformOrder(userDto);
        int cancelOrder = dashBoardService.getCancelOrder(userDto);

        response.put("total_order", totalOrder);
        response.put("today_order", todayOrder);
        response.put("processing_orders", processingOrders);
        response.put("conform_order", conformOrder);
        response.put("cancel_order", cancelOrder);

        return ResponseEntity.ok(response);
      } else {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/exportData/{name}")
  public ResponseEntity<Object> getAndExportToOrder(@RequestHeader(name = "Authorization") String authorizationHeader, @PathVariable("name") String name) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      List<ExcelTypeDto> entities = dashBoardService.findOrder(name);
      return new ResponseEntity<>(entities, HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/exportDataQty")
  public ResponseEntity<Object> getAndExportToOrderQty(@RequestHeader(name = "Authorization") String authorizationHeader) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      List<Object> entities = dashBoardService.findOrderQty();
      return new ResponseEntity<>(entities, HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/weekly_orders")
  public ResponseEntity<Object> weeklyChartOrders(
          @RequestHeader(name = "Authorization") String authorizationHeader) {

    try {

      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(
                TokenStatus.TOKEN_INVALID,
                HttpStatus.UNAUTHORIZED
        );
      }

      List<WeeklyUserOrderDto> entities =
              dashBoardService.findWeeklyOrder();

      return new ResponseEntity<>(
              entities,
              HttpStatus.OK
      );

    } catch (Exception e) {

      return new ResponseEntity<>(
              "Error retrieving weekly orders: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/daily_orders/{date}")
  public ResponseEntity<Object> DailyUsersOrders(@RequestHeader(name = "Authorization") String authorizationHeader,
                                                 @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }

      Map<String, Integer> entities = dashBoardService.DailyUsersOrders(date);
      return new ResponseEntity<>(entities, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
