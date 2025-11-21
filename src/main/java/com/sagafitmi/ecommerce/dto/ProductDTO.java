package com.sagafitmi.ecommerce.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    // current price as double for API compatibility (nullable)
    private Double price;
    // optional price history
    private List<PriceDTO> priceHistory;
}
