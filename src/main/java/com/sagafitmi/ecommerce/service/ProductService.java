package com.sagafitmi.ecommerce.service;

import java.util.List;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import java.math.BigDecimal;

public interface ProductService {
    // Define service methods here
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    ProductDTO updateProductPrice(Long id, BigDecimal price);
    void deleteProduct(Long id);
}
