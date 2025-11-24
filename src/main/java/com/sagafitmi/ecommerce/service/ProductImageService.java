package com.sagafitmi.ecommerce.service;

import com.sagafitmi.ecommerce.model.ProductImage;

public interface ProductImageService {

    /**
     * Crea una imagen asociada a un producto.
     * @param productId Id del producto al que se asocia la imagen
     * @param url URL de la imagen
     * @param mainImage true si debe marcarse como imagen principal
     * @return la entidad ProductImage creada
     */
    ProductImage createImage(Long productId, String url, boolean mainImage);

    /**
     * Crea una imagen a partir de un archivo multipart (jpg/png). Guarda el archivo
     * en disco usando la ruta configurada y guarda la URL en la entidad.
     * @param productId id del producto
     * @param file contenido multipart de la imagen
     * @param mainImage si debe marcarse como principal
     * @return la entidad ProductImage creada
     */
    ProductImage createImage(Long productId, org.springframework.web.multipart.MultipartFile file, boolean mainImage);

    /**
     * Borra una imagen por su id.
     * @param imageId id de la imagen a borrar
     */
    void deleteImage(Long imageId);

    /**
     * Asigna una imagen existente como la imagen principal del producto.
     * Debe desmarcar cualquier otra imagen principal previa del mismo producto.
     * @param productId id del producto
     * @param imageId id de la imagen a marcar como principal
     * @return la entidad ProductImage marcada como principal
     */
    ProductImage assignMainImage(Long productId, Long imageId);

    /**
     * Devuelve la lista de imágenes (DTOs) asociadas a un producto.
     * La conversión a DTO se realiza en la capa de servicio para mantener el
     * controlador como capa de presentación.
     * @param productId id del producto
     * @return lista de ProductImageDTO (vacía si no hay ninguna)
     */
    java.util.List<com.sagafitmi.ecommerce.dto.ProductImageDTO> getImagesByProduct(Long productId);

}
