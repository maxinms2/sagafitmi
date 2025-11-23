package com.sagafitmi.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sagafitmi.ecommerce.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}
