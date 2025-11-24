package com.sagafitmi.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findByProductAndMainImageTrue(Product product);

    @Query("select pi.url from ProductImage pi where pi.product.id = :productId and pi.mainImage = true")
    Optional<String> findMainImageUrlByProductId(@Param("productId") Long productId);

    @Query("select pi.product.id, pi.url from ProductImage pi where pi.product.id in :ids and pi.mainImage = true")
    List<Object[]> findMainImageUrlsByProductIds(@Param("ids") List<Long> ids);

}
