package com.sagafitmi.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricResponseDTO {
    private List<OrderItemMetricDTO> items;
    private BigDecimal grandTotal;
}
