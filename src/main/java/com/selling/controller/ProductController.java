package com.selling.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selling.dto.ProductDto;
import com.selling.service.ProductService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;

@CrossOrigin()
@RestController
@RequestMapping("/products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @Autowired
  private JWTTokenGenerator jwtTokenGenerator;

  @PostMapping
  public ResponseEntity<Object> saveProduct(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @RequestBody ProductDto productDTO) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      ProductDto savedProduct = productService.saveProduct(productDTO);
      return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>("Error saving product: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> getProductById(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable Integer id) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      ProductDto productDTO = productService.getProductById(Long.valueOf(id));
      if (productDTO != null) {
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving product: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping
  public ResponseEntity<Object> getAllProducts() {
    try {
      List<ProductDto> products = null;
      products = productService.getAllProducts();

      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Object> updateProduct(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable Integer id,
      @RequestBody ProductDto productDTO) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      ProductDto updatedProduct = productService.updateProduct(id, productDTO);
      if (updatedProduct != null) {
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error updating product: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteProduct(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable Integer id) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      boolean isDeleted = productService.deleteProduct(id);
      if (isDeleted) {
        return new ResponseEntity<>("Product disabled successfully", HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Error disabling product: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/search")
  public ResponseEntity<Object> searchProducts(
      @RequestHeader(name = "Authorization") String authorizationHeader,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      List<ProductDto> products = productService.searchProducts(name, minPrice, maxPrice);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Error searching products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
