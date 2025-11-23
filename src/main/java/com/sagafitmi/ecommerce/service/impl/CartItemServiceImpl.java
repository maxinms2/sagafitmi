package com.sagafitmi.ecommerce.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafitmi.ecommerce.dto.CartItemCreateDTO;
import com.sagafitmi.ecommerce.dto.CartItemDTO;
import com.sagafitmi.ecommerce.mapper.CartItemMapper;
import com.sagafitmi.ecommerce.model.CartItem;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.service.CartItemService;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartItemServiceImpl(CartItemRepository cartItemRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<CartItemDTO> getCartItemsByUser(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream().map(CartItemMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemDTO addCartItem(CartItemCreateDTO createDTO) {
        if (createDTO == null || createDTO.getUserId() == null || createDTO.getProductId() == null) {
            return null;
        }

        User user = userRepository.findById(createDTO.getUserId()).orElse(null);
        if (user == null) return null;

        Product product = productRepository.findById(createDTO.getProductId()).orElse(null);
        if (product == null) return null;

        // check existing item
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId()).orElse(null);
        if (item != null) {
            int add = createDTO.getQuantity() != null ? createDTO.getQuantity() : 1;
            item.setQuantity(item.getQuantity() + add);
            item = cartItemRepository.save(item);
            return CartItemMapper.toDTO(item);
        }

        CartItem newItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(createDTO.getQuantity() != null ? createDTO.getQuantity() : 1)
            .build();

        // store the product's current price at time of creation
        newItem.setCurrentPrice(product.getCurrentPriceValue());

        newItem = cartItemRepository.save(newItem);
        return CartItemMapper.toDTO(newItem);
    }

    @Override
    @Transactional
    public CartItemDTO updateQuantity(Long cartItemId, Integer quantity) {
        if (cartItemId == null || quantity == null || quantity < 0) return null;
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null) return null;
        item.setQuantity(quantity);
        item = cartItemRepository.save(item);
        return CartItemMapper.toDTO(item);
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        if (cartItemId == null) return;
        if (!cartItemRepository.existsById(cartItemId)) return;
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        if (userId == null) return;
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public List<CartItemDTO> findCartItemsWithPriceMismatch(Long userId) {
        java.util.List<com.sagafitmi.ecommerce.model.CartItem> items;
        if (userId == null) {
            items = cartItemRepository.findAll();
        } else {
            items = cartItemRepository.findByUserId(userId);
        }

        return items.stream()
                .filter(ci -> {
                    java.math.BigDecimal cartPrice = ci.getCurrentPrice();
                    java.math.BigDecimal prodPrice = ci.getProduct() != null ? ci.getProduct().getCurrentPriceValue() : null;
                    if (cartPrice == null && prodPrice == null) return false;
                    if (cartPrice == null || prodPrice == null) return true;
                    return cartPrice.compareTo(prodPrice) != 0;
                })
                .map(CartItemMapper::toDTO)
                .collect(Collectors.toList());
    }

}
