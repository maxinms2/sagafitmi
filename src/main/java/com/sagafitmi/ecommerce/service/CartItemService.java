package com.sagafitmi.ecommerce.service;

import java.util.List;

import com.sagafitmi.ecommerce.dto.CartItemCreateDTO;
import com.sagafitmi.ecommerce.dto.CartItemDTO;

public interface CartItemService {
    List<CartItemDTO> getCartItemsByUser(Long userId);

    CartItemDTO addCartItem(CartItemCreateDTO createDTO);

    CartItemDTO updateQuantity(Long cartItemId, Integer quantity);

    void removeCartItem(Long cartItemId);

    void clearCart(Long userId);

    /**
     * Devuelve los items del carrito cuyo `currentPrice` almacenado en el carrito
     * sea diferente al precio más reciente del producto relacionado.
     * Si `userId` es null, busca en todos los cart items.
     * @param userId id del usuario (opcional)
     * @return lista de `CartItemDTO` con discrepancias de precio
     */
    java.util.List<com.sagafitmi.ecommerce.dto.CartItemDTO> findCartItemsWithPriceMismatch(Long userId);
}
