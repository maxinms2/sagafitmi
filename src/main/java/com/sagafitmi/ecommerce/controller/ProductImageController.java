package com.sagafitmi.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
 
// RequestBody not used (multipart endpoint uses RequestPart)
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// CreateProductImageRequest not used after switching to multipart
import com.sagafitmi.ecommerce.dto.ProductImageDTO;
import com.sagafitmi.ecommerce.model.ProductImage;
import com.sagafitmi.ecommerce.service.ProductImageService;
import com.sagafitmi.ecommerce.exception.ResourceNotFoundException;
import com.sagafitmi.ecommerce.exception.BadRequestException;

@RestController
@RequestMapping("/api/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageDTO> createImage(@PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "mainImage", required = false, defaultValue = "false") Boolean mainImage
        ) {
        ProductImage img = productImageService.createImage(productId, file, mainImage);
        if (img == null) throw new ResourceNotFoundException("Producto no encontrado para id=" + productId);
        ProductImageDTO dto = toDto(img);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductImageDTO>> getImages(@PathVariable Long productId) {
        List<ProductImageDTO> dtos = productImageService.getImagesByProduct(productId);
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{productId}/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long productId, @PathVariable Long imageId) {
        // productId provided for URL semantics; service.deleteImage operates by imageId
        productImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/main/{productId}/{imageId}")
    public ResponseEntity<ProductImageDTO> assignMain(@PathVariable Long productId, @PathVariable Long imageId) {
        ProductImage img = productImageService.assignMainImage(productId, imageId);
        if (img == null) throw new ResourceNotFoundException("Imagen no encontrada: id=" + imageId);
        return ResponseEntity.ok(toDto(img));
    }

    private ProductImageDTO toDto(ProductImage img) {
        if (img == null) throw new BadRequestException("Imagen nula");
        return ProductImageDTO.builder()
                .id(img.getId())
                .url(img.getUrl())
                .mainImage(img.isMainImage())
                .productId(img.getProduct() != null ? img.getProduct().getId() : null)
                .build();
    }

}
