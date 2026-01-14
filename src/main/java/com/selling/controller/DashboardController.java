package com.selling.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selling.dto.UserDto;
import com.selling.dto.get.ExcelTypeDto;
import com.selling.service.DashBoardService;
import com.selling.service.OrderService;
import com.selling.util.ExcelExportService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;

import lombok.RequiredArgsConstructor;

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
    orderService.updateOrderDetails();
  }

  @GetMapping("/excel/{name}")
  public ResponseEntity<Object> exportToExcel(@RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable String name) {
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

  @GetMapping("/conform")
  public ResponseEntity<Object> ConformExport(@RequestHeader(name = "Authorization") String authorizationHeader) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      List<ExcelTypeDto> entities = dashBoardService.ConformOrder();
      return new ResponseEntity<>(entities, HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
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
        int conformOrder = dashBoardService.getConformOrder(userDto);
        int cancelOrder = dashBoardService.getCancelOrder(userDto);

        response.put("total_order", totalOrder);
        response.put("today_order", todayOrder);
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

}
