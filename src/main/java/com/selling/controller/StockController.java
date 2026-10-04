package com.selling.controller;

import com.selling.dto.StockDetailsDto;
import com.selling.dto.StockDto;
import com.selling.service.StockService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@CrossOrigin()
@RestController
@RequestMapping("/stockes")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private JWTTokenGenerator jwtTokenGenerator;

    @PostMapping
    public ResponseEntity<Object> savedStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody StockDetailsDto stockDetails) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            StockDto savedStock = stockService.saveStock(stockDetails);
            return new ResponseEntity<>(savedStock, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id,
            @RequestBody StockDetailsDto stockDetails) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            StockDto updatedStock = stockService.updateStock(id, stockDetails);
            if (updatedStock != null) {
                return new ResponseEntity<>(updatedStock, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating Stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            boolean isDeleted = stockService.deleteStock(id);
            if (isDeleted) {
                return new ResponseEntity<>("Stock details deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock details not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting Stock details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getStockDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Date date,
            @RequestParam(required = false) String month) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            List<StockDetailsDto> details = stockService.getStockDetails(type, status, date, month);
            return new ResponseEntity<>(details, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving stock details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stockQty")
    public ResponseEntity<Object> getStockDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            List<StockDto> entities = stockService.getStockQty();
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving stock details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}