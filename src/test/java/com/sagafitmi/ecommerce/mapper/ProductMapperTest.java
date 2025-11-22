package com.sagafitmi.ecommerce.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.model.Product;

class ProductMapperTest {

    @Test
    void toDTO_nullInput_returnsNull() {
        assertNull(ProductMapper.toDTO(null));
    }

    @Test
    void toDTO_mapsCurrentPriceAndFields() {
        Product p = new Product();
        p.setId(5L);
        p.setName("Test Product");
        p.setDescription("Desc");

        Price price = new Price();
        price.setId(11L);
        price.setPrice(new BigDecimal("19.90"));
        price.setCreatedAt(LocalDateTime.now());
        price.setProduct(p);
        p.getPrices().add(price);

        ProductDTO dto = ProductMapper.toDTO(p);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Test Product", dto.getName());
        assertEquals("Desc", dto.getDescription());
        assertEquals(new BigDecimal("19.90"), dto.getPrice());
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertNull(ProductMapper.toEntity(null));
    }

    @Test
    void toEntity_createsPriceWithScaleAndLink() {
        com.sagafitmi.ecommerce.dto.ProductDTO dto = new com.sagafitmi.ecommerce.dto.ProductDTO();
        dto.setId(7L);
        dto.setName("X");
        dto.setDescription("Y");
        dto.setPrice(new BigDecimal("12.345"));

        Product product = ProductMapper.toEntity(dto);

        assertNotNull(product);
        assertEquals(7L, product.getId());
        assertEquals("X", product.getName());
        assertEquals("Y", product.getDescription());

        assertNotNull(product.getPrices());
        assertEquals(1, product.getPrices().size());
        Price p = product.getPrices().get(0);
        assertEquals(new BigDecimal("12.34"), p.getPrice());
        assertEquals(product, p.getProduct());
        assertNotNull(p.getCreatedAt());
    }
}
