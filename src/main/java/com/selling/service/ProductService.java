package com.selling.service;

import com.selling.dto.ProductDto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductDto saveProduct(ProductDto productDTO);
    ProductDto getProductById(Long id);
    List<ProductDto> getAllProducts();
    ProductDto updateProduct(Integer id, ProductDto productDTO);
    boolean deleteProduct(Integer id);
    List<ProductDto> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDto> getAllProductsUserWise();

}
