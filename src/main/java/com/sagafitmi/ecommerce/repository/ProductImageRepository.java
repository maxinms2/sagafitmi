package com.sagafitmi.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findByProductAndMainImageTrue(Product product);

}
