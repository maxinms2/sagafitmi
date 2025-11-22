package com.sagafitmi.ecommerce.service.impl;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.mapper.ProductMapper;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        return ProductMapper.toDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findByNameIgnoreCase(productDTO.getName());
        if (existingProduct != null) {
            return null;
        }
        Product product = ProductMapper.toEntity(productDTO);

        product = productRepository.save(product);
        return ProductMapper.toDTO(product);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            return null;
        }
        Product product = ProductMapper.toEntity(productDTO);
        product.setId(id);
        product.setPrices(existingProduct.getPrices());
        product = productRepository.save(product);
        if(productDTO.getPrice() != null && 
           (existingProduct.getCurrentPriceValue() == null || 
            productDTO.getPrice().compareTo(existingProduct.getCurrentPriceValue()) != 0)) {
            updateProductPrice(id, productDTO.getPrice());
        }
        return ProductMapper.toDTO(product);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return;
        }
        productRepository.deleteById(id);
    }


    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    public ProductDTO updateProductPrice(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }

        // Crear nueva entrada de precio y añadirla al historial
        Price p = new Price();
        p.setPrice(newPrice.setScale(2, RoundingMode.HALF_EVEN));
        p.setCreatedAt(LocalDateTime.now());
        p.setProduct(product);
        product.getPrices().add(p);

        product = productRepository.save(product);
        return ProductMapper.toDTO(product);
    }

}
