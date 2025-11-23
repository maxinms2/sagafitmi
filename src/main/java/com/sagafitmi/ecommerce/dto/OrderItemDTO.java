package com.sagafitmi.ecommerce.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long id;
    private ProductDTO product;
    private Integer quantity;

    // Precio unitario al momento de confirmar la orden
    private BigDecimal price;
}
