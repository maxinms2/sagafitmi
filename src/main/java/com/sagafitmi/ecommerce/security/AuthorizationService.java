package com.sagafitmi.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.model.Role;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.service.OrderService;

@Component("authz")
public class AuthorizationService {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public AuthorizationService(UserRepository userRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    public boolean isAdmin() {
        User u = getCurrentUser();
        return u != null && u.getRole() == Role.ADMIN;
    }

    public boolean isCurrentUser(Long userId) {
        User u = getCurrentUser();
        return u != null && userId != null && u.getId().equals(userId);
    }

    public boolean isOrderOwner(Long orderId) {
        if (orderId == null) return false;
        OrderDTO dto = orderService.getOrderById(orderId);
        if (dto == null) return false;
        return isCurrentUser(dto.getUserId());
    }

    public boolean isOrderOwnerOrAdmin(Long orderId) {
        return isAdmin() || isOrderOwner(orderId);
    }

}
