package com.sagafitmi.ecommerce.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMetricRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    // "quantity" or "amount" - indica por qué campo ordenar de forma descendente
    private String sortBy;
    // Número máximo de productos a devolver (top N). Si es null, se usará un valor por defecto en el servicio.
    private Integer top;
}
