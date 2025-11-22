package com.sagafitmi.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.service.impl.ProductServiceImpl;

class ProductServiceImplTest {

    ProductRepository productRepository;
    ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        service = new ProductServiceImpl(productRepository);
    }

    @Test
    void getProductById_notFound_returnsNull() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());
        assertNull(service.getProductById(9L));
    }

    @Test
    void createProduct_duplicateName_returnsNull() {
        ProductDTO dto = new ProductDTO(); dto.setName("P");
        when(productRepository.findByNameIgnoreCase("P")).thenReturn(new Product());
        assertNull(service.createProduct(dto));
    }

    @Test
    void createProduct_savesAndReturnsDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setName("NewP");
        dto.setDescription("D");
        dto.setPrice(new BigDecimal("3.50"));

        when(productRepository.findByNameIgnoreCase("NewP")).thenReturn(null);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(22L);
            return p;
        });

        var res = service.createProduct(dto);
        assertNotNull(res);
        assertEquals(22L, res.getId());
    }

    @Test
    void updateProduct_notFound_returnsNull() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());
        assertNull(service.updateProduct(3L, new ProductDTO()));
    }

    @Test
    void updateProduct_priceChanged_callsSaveTwice() {
        Product existing = new Product();
        existing.setId(4L);
        Price pr = new Price(); pr.setPrice(new BigDecimal("1.00")); pr.setProduct(existing);
        existing.getPrices().add(pr);

        ProductDTO dto = new ProductDTO();
        dto.setName("X");
        dto.setPrice(new BigDecimal("2.00"));

        when(productRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        var res = service.updateProduct(4L, dto);

        // save invoked for update and for updateProductPrice
        verify(productRepository, atLeast(2)).save(any(Product.class));
        assertNotNull(res);
    }

    @Test
    void updateProductPrice_notFound_returnsNull() {
        when(productRepository.findById(100L)).thenReturn(Optional.empty());
        assertNull(service.updateProductPrice(100L, new BigDecimal("9.99")));
    }

    @Test
    void updateProductPrice_addsPrice_andReturnsDTO() {
        Product p = new Product(); p.setId(55L);
        when(productRepository.findById(55L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        var res = service.updateProductPrice(55L, new BigDecimal("7.123"));
        assertNotNull(res);
        assertEquals(new BigDecimal("7.12"), res.getPrice());
    }

    @Test
    void deleteProduct_checksExists() {
        when(productRepository.existsById(2L)).thenReturn(true);
        service.deleteProduct(2L);
        verify(productRepository).deleteById(2L);

        when(productRepository.existsById(3L)).thenReturn(false);
        service.deleteProduct(3L);
        verify(productRepository, never()).deleteById(3L);
    }

    @Test
    void getAllProducts_maps() {
        Product p = new Product(); p.setId(9L); p.setName("A");
        when(productRepository.findAll()).thenReturn(List.of(p));
        var list = service.getAllProducts();
        assertEquals(1, list.size());
    }
}
