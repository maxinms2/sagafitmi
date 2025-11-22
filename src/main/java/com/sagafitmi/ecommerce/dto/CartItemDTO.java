package com.sagafitmi.ecommerce.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long id;
    private Long userId;
    private ProductDTO product;
    private Integer quantity;

    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal currentPrice;
}
