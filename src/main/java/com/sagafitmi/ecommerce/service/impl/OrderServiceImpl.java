package com.sagafitmi.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.mapper.OrderMapper;
import com.sagafitmi.ecommerce.model.CartItem;
import com.sagafitmi.ecommerce.model.Order;
import com.sagafitmi.ecommerce.model.OrderItem;
import com.sagafitmi.ecommerce.model.OrderStatus;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.repository.OrderRepository;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartItemRepository cartItemRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long userId) {
        if (userId == null) return null;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        if (items == null || items.isEmpty()) return null;

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.NEW)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            // Usar el último precio del producto (historial), no el precio almacenado en el carrito
            BigDecimal price = ci.getProduct() != null && ci.getProduct().getCurrentPriceValue() != null
                ? ci.getProduct().getCurrentPriceValue()
                : BigDecimal.ZERO;
            int qty = ci.getQuantity() != null ? ci.getQuantity() : 0;

            OrderItem oi = OrderItem.builder()
                .order(order)
                .product(ci.getProduct())
                .quantity(qty)
                .price(price)
                .build();

            order.getItems().add(oi);

            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // limpiar carrito
        cartItemRepository.deleteByUserId(userId);

        return OrderMapper.toDTO(saved);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        if (orderId == null) return null;
        return orderRepository.findById(orderId).map(OrderMapper::toDTO).orElse(null);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(Long userId) {
        if (userId == null) return List.of();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        if (status == null) return List.of();
        return orderRepository.findByStatus(status).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        if (orderId == null || status == null) return null;
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return OrderMapper.toDTO(saved);
    }

}
