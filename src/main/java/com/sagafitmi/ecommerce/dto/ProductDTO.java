package com.sagafitmi.ecommerce.dto;

import java.util.List;
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
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    // current price (nullable). If present, must have up to 2 fractional digits and be non-negative
    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal price;
    // URL de la imagen principal (opcional)
    private String mainImageUrl;
    // optional price history
    //private List<PriceDTO> priceHistory;
}
