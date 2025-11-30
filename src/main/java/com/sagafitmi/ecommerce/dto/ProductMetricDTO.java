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
public class ProductMetricDTO {
    private Long productId;
    private String name;
    private String description;
    private Long quantitySold;
    private BigDecimal amountSold;
}
