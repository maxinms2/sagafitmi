// package com.sagafitmi.ecommerce.controller;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.math.BigDecimal;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import com.sagafitmi.ecommerce.dto.CartItemCreateDTO;
// import com.sagafitmi.ecommerce.dto.CartItemDTO;
// import com.sagafitmi.ecommerce.dto.CartItemUpdateDTO;
// import com.sagafitmi.ecommerce.dto.ProductDTO;
// import com.sagafitmi.ecommerce.service.CartItemService;

// class CartControllerTest {

//     CartItemService cartItemService;
//     CartController controller;

//     @BeforeEach
//     void setUp() {
//         cartItemService = mock(CartItemService.class);
//         controller = new CartController(cartItemService);
//     }

//     @Test
//     void getCartByUser_returnsItems() {
//         CartItemDTO c = new CartItemDTO(); c.setId(1L); c.setUserId(10L); c.setQuantity(2); c.setCurrentPrice(new BigDecimal("5.00"));
//         ProductDTO p = new ProductDTO(); p.setId(20L); p.setName("P"); c.setProduct(p);
//         when(cartItemService.getCartItemsByUser(10L)).thenReturn(List.of(c));

//         var resp = controller.getCartByUser(10L);
//         assertEquals(200, resp.getStatusCode().value());
//         assertEquals(1, resp.getBody().size());
//     }

//     @Test
//     void addToCart_badRequestWhenServiceReturnsNull() {
//         CartItemCreateDTO create = new CartItemCreateDTO(); create.setUserId(1L); create.setProductId(2L);
//         when(cartItemService.addCartItem(any())).thenReturn(null);

//         var resp = controller.addToCart(create);
//         assertEquals(400, resp.getStatusCode().value());
//     }

//     @Test
//     void addToCart_createdWhenServiceReturns() {
//         CartItemCreateDTO create = new CartItemCreateDTO(); create.setUserId(1L); create.setProductId(2L);
//         CartItemDTO out = new CartItemDTO(); out.setId(9L); out.setUserId(1L);
//         when(cartItemService.addCartItem(any())).thenReturn(out);

//         var resp = controller.addToCart(create);
//         assertEquals(201, resp.getStatusCode().value());
//         assertEquals(9L, resp.getBody().getId());
//     }

//     @Test
//     void updateQuantity_badRequestWhenMissingQuantity() {
//         CartItemUpdateDTO upd = new CartItemUpdateDTO();
//         var resp = controller.updateQuantity(5L, upd);
//         assertEquals(400, resp.getStatusCode().value());
//     }

//     @Test
//     void updateQuantity_notFoundWhenServiceNull() {
//         CartItemUpdateDTO upd = new CartItemUpdateDTO(); upd.setQuantity(3);
//         when(cartItemService.updateQuantity(7L, 3)).thenReturn(null);

//         var resp = controller.updateQuantity(7L, upd);
//         assertEquals(404, resp.getStatusCode().value());
//     }

//     @Test
//     void updateQuantity_okWhenServiceReturns() {
//         CartItemUpdateDTO upd = new CartItemUpdateDTO(); upd.setQuantity(4);
//         CartItemDTO out = new CartItemDTO(); out.setId(3L); out.setQuantity(4);
//         when(cartItemService.updateQuantity(3L, 4)).thenReturn(out);

//         var resp = controller.updateQuantity(3L, upd);
//         assertEquals(200, resp.getStatusCode().value());
//         assertEquals(4, resp.getBody().getQuantity());
//     }

//     @Test
//     void removeItem_and_clearCart_returnNoContent() {
//         var resp1 = controller.removeItem(12L);
//         var resp2 = controller.clearCart(10L);
//         assertEquals(204, resp1.getStatusCode().value());
//         assertEquals(204, resp2.getStatusCode().value());
//         verify(cartItemService).removeCartItem(12L);
//         verify(cartItemService).clearCart(10L);
//     }

// }
