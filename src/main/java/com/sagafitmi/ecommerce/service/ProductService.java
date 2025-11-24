package com.sagafitmi.ecommerce.service;

import java.util.List;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    // Define service methods here
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    ProductDTO updateProductPrice(Long id, BigDecimal price);
    void deleteProduct(Long id);

    Page<ProductDTO> searchProducts(String name, String description, Pageable pageable);
}
