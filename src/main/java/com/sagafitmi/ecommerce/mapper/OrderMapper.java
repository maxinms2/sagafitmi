package com.sagafitmi.ecommerce.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.model.Order;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;
        OrderDTO dto = OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();

        List.of();

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(OrderItemMapper::toDTO).collect(Collectors.toList()));
        }

        return dto;
    }

}
