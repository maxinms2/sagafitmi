package com.sagafitmi.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.model.OrderStatus;
import com.sagafitmi.ecommerce.service.OrderService;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("@authz.isCurrentUser(#userId)")
    public ResponseEntity<OrderDTO> createOrderFromCart(@PathVariable Long userId) {
        OrderDTO created = orderService.createOrderFromCart(userId);
        if (created == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("@authz.isOrderOwnerOrAdmin(#orderId)")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO dto = orderService.getOrderById(orderId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isCurrentUser(#userId)")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderDTO> list = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus st = OrderStatus.valueOf(status.toUpperCase());
            List<OrderDTO> list = orderService.getOrdersByStatus(st);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @PathVariable String status) {
        try {
            OrderStatus st = OrderStatus.valueOf(status.toUpperCase());
            OrderDTO updated = orderService.updateOrderStatus(orderId, st);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDTO>> searchOrders(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        OrderStatus st = null;

        try {
            if (startDate != null && !startDate.isBlank()) {
                LocalDate sd = LocalDate.parse(startDate);
                start = sd.atStartOfDay();
            }
            if (endDate != null && !endDate.isBlank()) {
                LocalDate ed = LocalDate.parse(endDate);
                end = ed.atTime(LocalTime.MAX);
            }
            if (status != null && !status.isBlank()) {
                st = OrderStatus.valueOf(status.toUpperCase());
            }
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        Page<OrderDTO> result = orderService.searchOrders(start, end, st, page, size);
        return ResponseEntity.ok(result);
    }

}
