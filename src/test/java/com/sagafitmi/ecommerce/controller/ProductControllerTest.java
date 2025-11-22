package com.sagafitmi.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sagafitmi.ecommerce.dto.PriceUpdateDTO;
import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.service.ProductService;

class ProductControllerTest {

    ProductService productService;
    ProductController controller;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        controller = new ProductController(productService);
    }

    @Test
    void getAllProducts_returnsList() {
        ProductDTO p = new ProductDTO(); p.setId(1L); p.setName("A"); p.setPrice(new BigDecimal("1.00"));
        when(productService.getAllProducts()).thenReturn(List.of(p));

        var resp = controller.getAllProducts();
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void getProductById_returnsOk() {
        ProductDTO p = new ProductDTO(); p.setId(2L); p.setName("B");
        when(productService.getProductById(2L)).thenReturn(p);

        var resp = controller.getProductById(2L);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(2L, resp.getBody().getId());
    }

    @Test
    void createProduct_conflictWhenNullReturned() {
        ProductDTO dto = new ProductDTO(); dto.setName("X");
        when(productService.createProduct(any())).thenReturn(null);

        var resp = controller.createProduct(dto);
        assertEquals(409, resp.getStatusCode().value());
    }

    @Test
    void createProduct_returnsCreated() {
        ProductDTO dto = new ProductDTO(); dto.setName("X");
        ProductDTO created = new ProductDTO(); created.setId(11L); created.setName("X");
        when(productService.createProduct(any())).thenReturn(created);

        var resp = controller.createProduct(dto);
        assertEquals(201, resp.getStatusCode().value());
        assertEquals(11L, resp.getBody().getId());
    }

    @Test
    void updateProductPrice_badRequestWhenMissingPrice() {
        PriceUpdateDTO bad = new PriceUpdateDTO();
        var resp = controller.updateProductPrice(1L, bad);
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void updateProductPrice_notFoundWhenServiceNull() {
        PriceUpdateDTO upd = new PriceUpdateDTO(); upd.setPrice(new BigDecimal("1.23"));
        when(productService.updateProductPrice(5L, upd.getPrice())).thenReturn(null);

        var resp = controller.updateProductPrice(5L, upd);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void updateProductPrice_okWhenUpdated() {
        PriceUpdateDTO upd = new PriceUpdateDTO(); upd.setPrice(new BigDecimal("2.50"));
        ProductDTO out = new ProductDTO(); out.setId(5L); out.setPrice(upd.getPrice());
        when(productService.updateProductPrice(5L, upd.getPrice())).thenReturn(out);

        var resp = controller.updateProductPrice(5L, upd);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(5L, resp.getBody().getId());
    }

}
