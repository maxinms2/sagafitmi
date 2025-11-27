package com.sagafitmi.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sagafitmi.ecommerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByNameIgnoreCase(String name);
        Product findByNameIgnoreCaseAndIdNot(String name, Long id);

    // @Query("SELECT p FROM Product p WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')))"
    //         + " AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%',:description,'%')))")
    // Page<Product> searchByNameAndDescription(@Param("name") String name,
    //         @Param("description") String description,
    //         Pageable pageable);

        @Query("SELECT p FROM Product p WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')))"
            + " AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%',:description,'%')))"
            + " ORDER BY LOWER(COALESCE(p.name, '')) ASC, LOWER(COALESCE(p.description, '')) ASC")
    Page<Product> searchByNameAndDescription(@Param("name") String name,
            @Param("description") String description,
            Pageable pageable);

}
