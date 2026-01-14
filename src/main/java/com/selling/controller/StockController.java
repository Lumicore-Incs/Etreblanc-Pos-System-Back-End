package com.selling.controller;

import com.selling.dto.StockDto;
import com.selling.service.StockService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody StockDto stockDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            StockDto savedStock = stockService.saveStock(stockDto);
            return new ResponseEntity<>(savedStock, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping
    public ResponseEntity<Object> getAllStock(@RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            List<StockDto> stock = null;
            stock = stockService.getAllStock();

            return new ResponseEntity<>(stock, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving Stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody StockDto stockDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            StockDto updatedStock = stockService.updateStock(id, stockDto);
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
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            boolean isDeleted = stockService.deleteStock(id);
            if (isDeleted) {
                return new ResponseEntity<>("Stock disabled successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("can't delete stock data", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error disabling Stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//=====================================
    @GetMapping("/{id}")
    public ResponseEntity<Object> getStockById(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            StockDto stockDto = stockService.getStockById(Long.valueOf(id));
            if (stockDto != null) {
                return new ResponseEntity<>(stockDto, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving Stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getData")
    public ResponseEntity<Object> getAllStockByType(@RequestHeader(name = "Authorization") String authorizationHeader, @RequestBody StockDto stockDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }
            List<StockDto> stock = null;
            stock = stockService.getAllStockByType(stockDto.getType());

            return new ResponseEntity<>(stock, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving Stock: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
