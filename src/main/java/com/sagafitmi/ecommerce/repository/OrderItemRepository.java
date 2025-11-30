package com.sagafitmi.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sagafitmi.ecommerce.dto.ProductMetricDTO;
import com.sagafitmi.ecommerce.model.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {

    List<OrderItem> findByOrderId(Long orderId);

    // existe al menos un order item para un producto
    boolean existsByProductId(Long productId);

    // Aggregation queries pushed to DB for performance: group by product and sum quantity and amount
    @Query("SELECT new com.sagafitmi.ecommerce.dto.ProductMetricDTO(p.id, p.name, p.description, SUM(i.quantity), SUM(i.price * i.quantity)) "
        + "FROM OrderItem i JOIN i.product p JOIN i.order o "
        + "WHERE o.createdAt >= :start AND o.createdAt <= :end "
        + "GROUP BY p.id, p.name, p.description "
        + "ORDER BY SUM(i.quantity) DESC, SUM(i.price * i.quantity) DESC")
    List<ProductMetricDTO> findProductMetricsOrderByQuantityDesc(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT new com.sagafitmi.ecommerce.dto.ProductMetricDTO(p.id, p.name, p.description, SUM(i.quantity), SUM(i.price * i.quantity)) "
        + "FROM OrderItem i JOIN i.product p JOIN i.order o "
        + "WHERE o.createdAt >= :start AND o.createdAt <= :end "
        + "GROUP BY p.id, p.name, p.description "
        + "ORDER BY SUM(i.price * i.quantity) DESC, SUM(i.quantity) DESC")
    List<ProductMetricDTO> findProductMetricsOrderByAmountDesc(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

}
