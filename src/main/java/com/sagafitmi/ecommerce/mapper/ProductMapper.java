package com.sagafitmi.ecommerce.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.sagafitmi.ecommerce.dto.PriceDTO;
import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.model.Product;

public class ProductMapper {
    public static ProductDTO toDTO(Product product) {
        if (product == null)
            return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());

        BigDecimal current = product.getCurrentPriceValue();
        dto.setPrice(current);

        // if (product.getPrices() != null) {
        // List<PriceDTO> history = product.getPrices().stream()
        // .map(p -> {
        // PriceDTO pd = new PriceDTO();
        // pd.setId(p.getId());
        // pd.setPrice(p.getPrice());
        // pd.setCreatedAt(p.getCreatedAt());
        // return pd;
        // })
        // .collect(Collectors.toList());
        // //dto.setPriceHistory(history);
        // }

        return dto;
    }

    public static Product toEntity(ProductDTO dto) {
        if (dto == null)
            return null;
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());

        if (dto.getPrice() != null) {
            Price p = new Price();
            p.setPrice(dto.getPrice().setScale(2, RoundingMode.HALF_EVEN));
            p.setCreatedAt(LocalDateTime.now());
            p.setProduct(product);
            product.getPrices().add(p);
        }

        return product;
    }
}
