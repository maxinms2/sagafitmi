package com.sagafitmi.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sagafitmi.ecommerce.model.Price;

public interface PriceRepository extends JpaRepository<Price, Long> {

    // Obtiene el precio más reciente por product_id para la lista de ids.
    // Retorna una lista de Object[] donde [0]=product_id, [1]=price
    @Query(value = "SELECT product_id, price FROM ("
            + " SELECT product_id, price, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY created_at DESC) rn"
            + " FROM prices WHERE product_id IN (:ids)"
            + ") t WHERE t.rn = 1", nativeQuery = true)
    List<Object[]> findLatestPricesByProductIds(@Param("ids") List<Long> ids);

}
