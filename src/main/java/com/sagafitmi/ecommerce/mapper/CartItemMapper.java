package com.sagafitmi.ecommerce.mapper;

import com.sagafitmi.ecommerce.dto.CartItemDTO;
import com.sagafitmi.ecommerce.model.CartItem;

public class CartItemMapper {
    public static CartItemDTO toDTO(CartItem item) {
        if (item == null) return null;
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUser() != null ? item.getUser().getId() : null);
        dto.setProduct(ProductMapper.toDTO(item.getProduct()));
        dto.setQuantity(item.getQuantity());
        dto.setCurrentPrice(item.getCurrentPrice());
        return dto;
    }

}
