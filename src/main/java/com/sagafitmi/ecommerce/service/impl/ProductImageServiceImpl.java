package com.sagafitmi.ecommerce.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.ProductImage;
import com.sagafitmi.ecommerce.repository.ProductImageRepository;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.service.ProductImageService;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductImageServiceImpl(ProductRepository productRepository,
                                   ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public ProductImage createImage(Long productId, String url, boolean mainImage) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;

        if (mainImage) {
            // Desmarcar la imagen principal actual si existe
            Optional<ProductImage> currentMain = productImageRepository.findByProductAndMainImageTrue(product);
            if (currentMain.isPresent()) {
                ProductImage ci = currentMain.get();
                ci.setMainImage(false);
                productImageRepository.save(ci);
            }
        }

        ProductImage img = ProductImage.builder()
                .url(url)
                .mainImage(mainImage)
                .product(product)
                .build();

        return productImageRepository.save(img);
    }

    @Override
    public void deleteImage(Long imageId) {
        if (!productImageRepository.existsById(imageId)) return;
        productImageRepository.deleteById(imageId);
    }

    @Override
    public ProductImage assignMainImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) return null;

        Product product = image.getProduct();
        if (product == null || !product.getId().equals(productId)) {
            // Imagen no pertenece al producto indicado
            return null;
        }

        // Desmarcar la principal actual si existe y es distinta
        Optional<ProductImage> currentMain = productImageRepository.findByProductAndMainImageTrue(product);
        if (currentMain.isPresent()) {
            ProductImage ci = currentMain.get();
            if (!ci.getId().equals(image.getId())) {
                ci.setMainImage(false);
                productImageRepository.save(ci);
            }
        }

        image.setMainImage(true);
        return productImageRepository.save(image);
    }

}
