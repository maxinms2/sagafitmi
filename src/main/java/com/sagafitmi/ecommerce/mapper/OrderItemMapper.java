package com.sagafitmi.ecommerce.mapper;

import com.sagafitmi.ecommerce.dto.OrderItemDTO;
import com.sagafitmi.ecommerce.model.OrderItem;

public class OrderItemMapper {

    public static OrderItemDTO toDTO(OrderItem item) {
        if (item == null) return null;
        return OrderItemDTO.builder()
                .id(item.getId())
                .product(ProductMapper.toDTO(item.getProduct()))
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

}
