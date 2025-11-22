package com.sagafitmi.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sagafitmi.ecommerce.dto.CartItemCreateDTO;
import com.sagafitmi.ecommerce.dto.CartItemDTO;
import com.sagafitmi.ecommerce.model.CartItem;
import com.sagafitmi.ecommerce.model.Price;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.repository.ProductRepository;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.service.impl.CartItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CartItemServiceImpl service;

    User user;
    Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setName("U");

        product = new Product();
        product.setId(20L);
        product.setName("P");
        // attach a current price
        Price pr = new Price();
        pr.setPrice(new BigDecimal("5.00"));
        pr.setCreatedAt(LocalDateTime.now());
        pr.setProduct(product);
        product.getPrices().add(pr);
    }

    @Test
    void getCartItemsByUser_mapsToDTOs() {
        CartItem item = CartItem.builder().id(1L).user(user).product(product).quantity(2).currentPrice(new BigDecimal("5.00")).build();
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(item));

        List<CartItemDTO> dtos = service.getCartItemsByUser(10L);

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(10L, dtos.get(0).getUserId());
        assertEquals(2, dtos.get(0).getQuantity());
    }

    @Test
    void addCartItem_existingItem_incrementsQuantity() {
        CartItem existing = CartItem.builder().id(2L).user(user).product(product).quantity(2).currentPrice(new BigDecimal("5.00")).build();
        CartItemCreateDTO create = new CartItemCreateDTO();
        create.setUserId(10L);
        create.setProductId(20L);
        create.setQuantity(3);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(10L, 20L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        var dto = service.addCartItem(create);

        assertNotNull(dto);
        assertEquals(5, dto.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void addCartItem_newItem_savesWithCurrentPrice() {
        CartItemCreateDTO create = new CartItemCreateDTO();
        create.setUserId(10L);
        create.setProductId(20L);
        create.setQuantity(4);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(10L, 20L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> {
            CartItem c = i.getArgument(0);
            c.setId(33L);
            return c;
        });

        var dto = service.addCartItem(create);

        assertNotNull(dto);
        assertEquals(33L, dto.getId());
        assertEquals(new BigDecimal("5.00"), dto.getCurrentPrice());
        assertEquals(4, dto.getQuantity());
    }

    @Test
    void updateQuantity_valid_updatesAndReturnsDTO() {
        CartItem item = CartItem.builder().id(50L).user(user).product(product).quantity(1).currentPrice(new BigDecimal("5.00")).build();
        when(cartItemRepository.findById(50L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        var dto = service.updateQuantity(50L, 7);
        assertNotNull(dto);
        assertEquals(7, dto.getQuantity());
    }

    @Test
    void removeCartItem_existing_deletes() {
        when(cartItemRepository.existsById(100L)).thenReturn(true);

        service.removeCartItem(100L);

        verify(cartItemRepository).deleteById(100L);
    }

    @Test
    void clearCart_callsDeleteByUserId() {
        service.clearCart(10L);
        verify(cartItemRepository).deleteByUserId(10L);
    }

    @Test
    void addCartItem_nullInput_returnsNull() {
        assertNull(service.addCartItem(null));
        verifyNoInteractions(userRepository, productRepository, cartItemRepository);
    }

    @Test
    void addCartItem_missingIds_returnsNull() {
        var create = new CartItemCreateDTO();
        create.setQuantity(2);
        assertNull(service.addCartItem(create));
        verifyNoInteractions(userRepository, productRepository, cartItemRepository);
    }

    @Test
    void addCartItem_userNotFound_returnsNull() {
        var create = new CartItemCreateDTO();
        create.setUserId(999L);
        create.setProductId(20L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertNull(service.addCartItem(create));
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(productRepository, cartItemRepository);
    }

    @Test
    void addCartItem_productNotFound_returnsNull() {
        var create = new CartItemCreateDTO();
        create.setUserId(10L);
        create.setProductId(999L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertNull(service.addCartItem(create));
        verify(userRepository).findById(10L);
        verify(productRepository).findById(999L);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void addCartItem_existingItem_withNullQuantity_addsOne() {
        CartItem existing = CartItem.builder().id(2L).user(user).product(product).quantity(2).currentPrice(new BigDecimal("5.00")).build();
        CartItemCreateDTO create = new CartItemCreateDTO();
        create.setUserId(10L);
        create.setProductId(20L);
        // quantity intentionally null -> should default to 1

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(10L, 20L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        var dto = service.addCartItem(create);

        assertNotNull(dto);
        assertEquals(3, dto.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void updateQuantity_nullOrNegative_returnsNull() {
        assertNull(service.updateQuantity(null, 5));
        assertNull(service.updateQuantity(1L, null));
        assertNull(service.updateQuantity(1L, -3));
    }

    @Test
    void removeCartItem_nullOrNonExisting_doesNothing() {
        service.removeCartItem(null);
        verifyNoInteractions(cartItemRepository);

        when(cartItemRepository.existsById(55L)).thenReturn(false);
        service.removeCartItem(55L);
        verify(cartItemRepository).existsById(55L);
        verify(cartItemRepository, never()).deleteById(55L);
    }

    @Test
    void clearCart_null_doesNothing() {
        service.clearCart(null);
        verify(cartItemRepository, never()).deleteByUserId(anyLong());
    }

}
