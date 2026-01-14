package com.selling.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.selling.dto.ProductDto;
import com.selling.model.Product;
import com.selling.repository.ProductRepo;
import com.selling.service.ProductService;
import com.selling.util.MapperService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

  @Autowired
  private ProductRepo productRepository;
  private final MapperService mapperService;

  @Override
  public ProductDto saveProduct(ProductDto productDTO) {
    Product product = new Product();
    product.setName(productDTO.getName());
    product.setPrice(productDTO.getPrice());
    product.setStatus("ACTIVE"); // Default status active
    product.setSerialPrefix(productDTO.getSerialPrefix());
    product.setShortName(productDTO.getShortName());

    productRepository.save(product);
    return entityToDTO(product);
  }

  @Override
  public ProductDto getProductById(Long id) {
    Optional<Product> product = productRepository.findById(id);
    return product.map(this::entityToDTO).orElse(null);
  }

  @Override
  public List<ProductDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return products.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductDto> getAllProductsUserWise() {
    List<Product> products = productRepository.findAllByStatus("ACTIVE");
    return products.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public ProductDto updateProduct(Integer id, ProductDto productDTO) {
    Optional<Product> productOptional = productRepository.findById(Long.valueOf(id));
    if (productOptional.isPresent()) {
      Product product = productOptional.get();
      product.setName(productDTO.getName());
      product.setPrice(productDTO.getPrice());
      product.setStatus(productDTO.getStatus());
      product.setSerialPrefix(productDTO.getSerialPrefix());
      product.setShortName(productDTO.getShortName());

      Product updatedProduct = productRepository.save(product);
      return entityToDTO(updatedProduct);
    }
    return null;
  }

  @Override
  public boolean deleteProduct(Integer id) {
    Optional<Product> productOptional = productRepository.findById(Long.valueOf(id));
    if (productOptional.isPresent()) {
      Product product = productOptional.get();
      product.setStatus("INACTIVE");
      productRepository.save(product);
      return true;
    }
    return false;
  }

  @Override
  public List<ProductDto> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice) {
    List<Product> products;

    if (name != null && minPrice != null && maxPrice != null) {
      products = productRepository.findByNameContainingAndPriceBetweenAndStatus(
          name, minPrice, maxPrice, "ACTIVE");
    } else if (name != null && minPrice != null) {
      products = productRepository.findByNameContainingAndPriceGreaterThanEqualAndStatus(
          name, minPrice, "ACTIVE");
    } else if (name != null && maxPrice != null) {
      products = productRepository.findByNameContainingAndPriceLessThanEqualAndStatus(
          name, maxPrice, "ACTIVE");
    } else if (minPrice != null && maxPrice != null) {
      products = productRepository.findByPriceBetweenAndStatus(minPrice, maxPrice, "ACTIVE");
    } else if (name != null) {
      products = productRepository.findByNameContainingAndStatus(name, "ACTIVE");
    } else if (minPrice != null) {
      products = productRepository.findByPriceGreaterThanEqualAndStatus(minPrice, "ACTIVE");
    } else if (maxPrice != null) {
      products = productRepository.findByPriceLessThanEqualAndStatus(maxPrice, "ACTIVE");
    } else {
      products = productRepository.findAllByStatus("ACTIVE");
    }

    return products.stream()
        .map(this::entityToDTO)
        .collect(Collectors.toList());
  }

  private ProductDto entityToDTO(Product product) {
    return (product == null) ? null : mapperService.map(product, ProductDto.class);
  }
}
