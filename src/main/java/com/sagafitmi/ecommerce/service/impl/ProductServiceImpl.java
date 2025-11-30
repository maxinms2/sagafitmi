package com.sagafitmi.ecommerce.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.sagafitmi.ecommerce.dto.ProductDTO;
import com.sagafitmi.ecommerce.exception.ProductCreateException;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.mapper.ProductMapper;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.repository.PriceRepository;
import com.sagafitmi.ecommerce.repository.ProductImageRepository;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.repository.OrderItemRepository;
import com.sagafitmi.ecommerce.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import com.sagafitmi.ecommerce.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ProductImageRepository productImageRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageService productImageService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
            PriceRepository priceRepository,
            ProductImageRepository productImageRepository,
            CartItemRepository cartItemRepository,
            OrderItemRepository orderItemRepository,
            ProductImageService productImageService) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.productImageRepository = productImageRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productImageService = productImageService;
    }

    // Compat constructor para tests existentes y usos previos
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.priceRepository = null;
        this.productImageRepository = null;
        this.cartItemRepository = null;
        this.orderItemRepository = null;
        this.productImageService = null;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;
        // Si los repositorios por lote no están inyectados (tests unitarios antiguos),
        // caer de regreso al mapeo simple para no romper tests.
        if (priceRepository == null || productImageRepository == null) {
            return ProductMapper.toDTO(product);
        }

        // obtener imagen principal y precio más reciente eficientemente
        String mainImageUrl = productImageRepository.findMainImageUrlByProductId(id).orElse(null);

        java.math.BigDecimal price = null;
        List<Object[]> priceRows = priceRepository.findLatestPricesByProductIds(List.of(id));
        if (priceRows != null && !priceRows.isEmpty()) {
            Object[] row = priceRows.get(0);
            if (row != null && row.length >= 2 && row[1] != null) {
                price = new java.math.BigDecimal(row[1].toString());
            }
        }

        return ProductMapper.toSummaryDTO(product, price, mainImageUrl);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findByNameIgnoreCaseAndDescriptionIgnoreCaseAndIdNot(productDTO.getName(), productDTO.getDescription(), null);
        if (existingProduct != null) {
            throw new ProductCreateException("Ya existe un producto con el mismo nombre y descripción");
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
        // Validar nombre único (case-insensitive) antes de aplicar cambios.
        // Si existe otro producto con el mismo nombre (ignorando mayúsculas/minúsculas), no aplicar la modificación.
        String newName = productDTO.getName();
        if (newName != null && !newName.isBlank()) {
            // Buscar otro producto con el mismo nombre pero distinto id (más eficiente y directo)
            Product other = productRepository.findByNameIgnoreCaseAndDescriptionIgnoreCaseAndIdNot(newName, productDTO.getDescription(), id);
            if (other != null) {
                // Nombre ya usado por otro producto -> abortar actualización
                throw new ProductCreateException("Ya existe un producto con el mismo nombre y descripción");
            }
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

        // Si no se inyectaron repositorios adicionales (compatibilidad con tests), conservar comportamiento anterior
        if (cartItemRepository == null || orderItemRepository == null || productImageService == null || productImageRepository == null) {
            productRepository.deleteById(id);
            return;
        }

        // No eliminar si hay referencias en carrito u órdenes
        if (cartItemRepository.existsByProductId(id) || orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException("No se puede eliminar producto: existen referencias en carrito u órdenes");
        }

        // Eliminar imágenes físicas y registros relacionados en product_images
        java.util.List<com.sagafitmi.ecommerce.model.ProductImage> imgs = productImageRepository.findByProductId(id);
        if (imgs != null && !imgs.isEmpty()) {
            for (com.sagafitmi.ecommerce.model.ProductImage img : imgs) {
                try {
                    productImageService.deleteImage(img.getId());
                } catch (Exception ex) {
                    // Registrar y continuar: fallo al borrar fichero o registro concreto no debe dejar inconsistencia
                    // Si quieres cambiar a fallo duro, lanza la excepción.
                }
            }
        }

        // El borrado del producto cascada eliminará los precios (cascade = ALL)
        productRepository.deleteById(id);
    }


    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        if (products == null || products.isEmpty()) return List.of();

        // Si no están disponibles los repositorios por lote, caer al mapeo previo (útil para tests unitarios)
        if (priceRepository == null || productImageRepository == null) {
            return products.stream()
                .map(ProductMapper::toDTO)
                .toList();
        }

        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());

        // precios más recientes por lote
        List<Object[]> priceRows = priceRepository.findLatestPricesByProductIds(ids);
        Map<Long, java.math.BigDecimal> priceMap = priceRows == null ? Map.of()
            : priceRows.stream()
                .filter(r -> r != null && r.length >= 2 && r[0] != null && r[1] != null)
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> new java.math.BigDecimal(r[1].toString())));

        // mainImageUrl por lote
        List<Object[]> imageRows = productImageRepository.findMainImageUrlsByProductIds(ids);
        Map<Long, String> imageMap = imageRows == null ? Map.of()
            : imageRows.stream()
                .filter(r -> r != null && r.length >= 2 && r[0] != null)
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> r[1] == null ? null : r[1].toString()));

        return products.stream()
            .map(p -> ProductMapper.toSummaryDTO(p, priceMap.get(p.getId()), imageMap.get(p.getId())))
            .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> searchProducts(String name, String description, Pageable pageable) {
        Page<Product> page = productRepository.searchByNameAndDescription(name, description, pageable);
        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page == null ? 0 : page.getTotalElements());
        }

        // Si no están disponibles los repositorios por lote, caer al mapeo previo (útil para tests unitarios)
        if (priceRepository == null || productImageRepository == null) {
            List<ProductDTO> dtos = page.getContent().stream().map(ProductMapper::toDTO).collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, page.getTotalElements());
        }

        List<Long> ids = page.getContent().stream().map(Product::getId).collect(Collectors.toList());

        // precios más recientes por lote
        List<Object[]> priceRows = priceRepository.findLatestPricesByProductIds(ids);
        Map<Long, java.math.BigDecimal> priceMap = priceRows == null ? Map.of()
            : priceRows.stream()
                .filter(r -> r != null && r.length >= 2 && r[0] != null && r[1] != null)
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> new java.math.BigDecimal(r[1].toString())));

        // mainImageUrl por lote
        List<Object[]> imageRows = productImageRepository.findMainImageUrlsByProductIds(ids);
        Map<Long, String> imageMap = imageRows == null ? Map.of()
            : imageRows.stream()
                .filter(r -> r != null && r.length >= 2 && r[0] != null)
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> r[1] == null ? null : r[1].toString()));

        List<ProductDTO> dtos = page.getContent().stream()
            .map(p -> ProductMapper.toSummaryDTO(p, priceMap.get(p.getId()), imageMap.get(p.getId())))
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
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
