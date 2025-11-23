package com.sagafitmi.ecommerce.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sagafitmi.ecommerce.dto.OrderItemDTO;
import com.sagafitmi.ecommerce.mapper.OrderItemMapper;
import com.sagafitmi.ecommerce.repository.OrderItemRepository;
import com.sagafitmi.ecommerce.service.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<OrderItemDTO> getItemsByOrder(Long orderId) {
        if (orderId == null) return List.of();
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO getOrderItemById(Long id) {
        if (id == null) return null;
        return orderItemRepository.findById(id).map(OrderItemMapper::toDTO).orElse(null);
    }

}
