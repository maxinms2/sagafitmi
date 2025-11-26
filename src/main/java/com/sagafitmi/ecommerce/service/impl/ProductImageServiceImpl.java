package com.sagafitmi.ecommerce.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.ProductImage;
import com.sagafitmi.ecommerce.dto.ProductImageDTO;
import com.sagafitmi.ecommerce.repository.ProductImageRepository;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.service.ProductImageService;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Value("${PRODUCT_IMAGE_BASE_PATH:C:/imgs}")
    private String basePath;

    @Value("${PRODUCT_IMAGE_BASE_URI:/images}")
    private String baseUri;

    public ProductImageServiceImpl(ProductRepository productRepository,
                                   ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public ProductImage createImage(Long productId, String url, boolean mainImage) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;
        // Nota: no se impone un límite rígido aquí. Si quieres limitar el número
        // de imágenes por producto, considera validar en la capa de controlador
        // o exponer una propiedad configurable (application.yml) y comprobarla
        // antes de crear la nueva imagen.

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
                .build();

        // Mantener la relación bidireccional usando el helper
        product.addImage(img);

        return productImageRepository.save(img);
    }

    @Override
    public java.util.List<ProductImageDTO> getImagesByProduct(Long productId) {
        if (productId == null) return java.util.Collections.emptyList();
        java.util.List<ProductImage> imgs = productImageRepository.findByProductId(productId);
        return imgs.stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    private ProductImageDTO toDto(ProductImage img) {
        if (img == null) return null;
        return ProductImageDTO.builder()
                .id(img.getId())
                .url(img.getUrl())
                .mainImage(img.isMainImage())
                .productId(img.getProduct() != null ? img.getProduct().getId() : null)
                .build();
    }

    @Override
    public ProductImage createImage(Long productId, MultipartFile file, boolean mainImage) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichero vacío");
        }

        String contentType = file.getContentType();
        String ext = null;
        if ("image/png".equalsIgnoreCase(contentType)) {
            ext = ".png";
        } else if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) {
            ext = ".jpg";
        } else {
            // intentar deducir por nombre de archivo
            String name = file.getOriginalFilename();
            if (name != null && (name.toLowerCase().endsWith(".png"))) ext = ".png";
            else if (name != null && (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"))) ext = ".jpg";
        }

        if (ext == null) {
            throw new IllegalArgumentException("Tipo de imagen no soportado. Solo JPG o PNG.");
        }

        try {
            Path dir = Paths.get(basePath);
            Files.createDirectories(dir);

            String filename = java.util.UUID.randomUUID().toString() + ext;
            Path target = dir.resolve(filename);
            // Guardar fichero
            Files.copy(file.getInputStream(), target);

            logger.info("Imagen guardada en: " + target.toAbsolutePath().toString());

            // Formar URL/Path guardada en la entidad (basePath + filename)
            //String url = target.toAbsolutePath().toString();
            String url = baseUri + "/" + filename;

            // Si mainImage se solicita, desmarcar la actual
            if (mainImage) {
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
                    .build();

            product.addImage(img);
            return productImageRepository.save(img);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    @Override
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) return;

        Product product = image.getProduct();
        boolean wasMain = image.isMainImage();

        // Intenta eliminar el fichero físico si la URL parece un path local
        String url = image.getUrl();
        if (url != null && !url.isBlank()) {
            try {
                java.nio.file.Path p = Paths.get(url);
                try {
                    java.nio.file.Files.deleteIfExists(p);
                } catch (Exception ex) {
                    // No detener la operación si no se puede borrar el fichero físico
                    logger.error("No se pudo borrar fichero de imagen: {}", ex.getMessage());
                }
            } catch (Exception ex) {
                // Si no es un path válido (p. ej. URL http://...), ignorar
            }
        }

        // Mantener consistencia en la relación: eliminar de la colección del producto
        if (product != null) {
            // Crear una copia para calcular la 'imagen anterior' acorde al orden de subida
            // Remover de la entidad Product (orphanRemoval está activado)
            product.removeImage(image);

            // Si la imagen eliminada era la principal, asignar la última imagen restante (la más reciente)
            if (wasMain) {
                java.util.List<ProductImage> remaining = new java.util.ArrayList<>(product.getImages());
                if (!remaining.isEmpty()) {
                    // Asegurarse de que no haya otra marcada como main
                    for (ProductImage pi : remaining) {
                        if (pi.isMainImage()) {
                            pi.setMainImage(false);
                            productImageRepository.save(pi);
                        }
                    }

                    ProductImage candidate = remaining.get(remaining.size() - 1);
                    if (candidate != null) {
                        candidate.setMainImage(true);
                        productImageRepository.save(candidate);
                    }
                }
            }
        }

        // Finalmente eliminar la entidad de la BD (si no se eliminó por orphanRemoval)
        try {
            if (product == null) {
                // imagen sin producto, borrar directamente
                productImageRepository.deleteById(imageId);
            } else {
                // Si orphanRemoval está activo la eliminación puede ya estar en la cola; asegurarse
                if (productImageRepository.existsById(imageId)) {
                    productImageRepository.deleteById(imageId);
                }
            }
        } catch (Exception ex) {
            // ignorar errores de borrado de persistencia aquí
            throw ex;
        }
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
