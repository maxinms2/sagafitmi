package com.sagafitmi.ecommerce.service;

import java.util.List;

import com.sagafitmi.ecommerce.dto.OrderItemDTO;

public interface OrderItemService {

    /**
     * Obtener los items que pertenecen a una orden.
     * No se permiten acciones de crear o eliminar items aquí: esas operaciones
     * se realizan sobre el carrito de compras antes de confirmar la orden.
     * @param orderId id de la orden
     * @return lista de items de la orden
     */
    List<OrderItemDTO> getItemsByOrder(Long orderId);

    OrderItemDTO getOrderItemById(Long id);

}
