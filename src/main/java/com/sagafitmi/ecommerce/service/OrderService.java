package com.sagafitmi.ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.model.OrderStatus;

public interface OrderService {

    /**
     * Crea una orden a partir del carrito de compras del usuario.
     * Solo se requiere el id del usuario. El proceso tomará los items
     * del carrito relacionado y generará la orden y la factura.
     * @param userId id del usuario que confirma la compra
     * @return la orden creada
     */
    OrderDTO createOrderFromCart(Long userId);

    OrderDTO getOrderById(Long orderId);

    List<OrderDTO> getOrdersByUser(Long userId);

    List<OrderDTO> getOrdersByStatus(OrderStatus status);

    /**
     * Busca órdenes filtrando opcionalmente por rango de fechas y estado.
     * Retorna una página de resultados.
     * @param start fecha/hora inicial (inclusive) o null
     * @param end fecha/hora final (inclusive) o null
     * @param status estado a filtrar o null
     * @param page número de página (0-index)
     * @param size tamaño de página
     * @return página de OrderDTO
     */
    Page<OrderDTO> searchOrders(LocalDateTime start, LocalDateTime end, OrderStatus status, int page, int size);

    /**
     * Actualiza el estado de la orden (ej. para marcar como PROCESSING, COMPLETED, CANCELLED).
     * @param orderId id de la orden
     * @param status nuevo estado
     * @return orden actualizada
     */
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);

}
