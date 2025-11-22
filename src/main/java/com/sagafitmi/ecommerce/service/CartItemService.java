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
}
