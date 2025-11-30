package com.sagafitmi.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sagafitmi.ecommerce.dto.CartItemCreateDTO;
import com.sagafitmi.ecommerce.dto.CartItemDTO;
import com.sagafitmi.ecommerce.dto.CartItemUpdateDTO;
import com.sagafitmi.ecommerce.service.CartItemService;
import com.sagafitmi.ecommerce.exception.BadRequestException;
import com.sagafitmi.ecommerce.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartItemService cartItemService;

	public CartController(CartItemService cartItemService) {
		this.cartItemService = cartItemService;
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<CartItemDTO>> getCartByUser(@PathVariable Long userId) {
		List<CartItemDTO> items = cartItemService.getCartItemsByUser(userId);
		return ResponseEntity.ok(items);
	}

	@PostMapping
	public ResponseEntity<CartItemDTO> addToCart(@RequestBody CartItemCreateDTO createDTO) {
		CartItemDTO added = cartItemService.addCartItem(createDTO);
		if (added == null) throw new BadRequestException("No se pudo añadir el item al carrito: datos inválidos o recurso inexistente");
		return ResponseEntity.status(HttpStatus.CREATED).body(added);
	}

	@PutMapping("/{id}")//ESte tal vez no tenga sentido
	public ResponseEntity<CartItemDTO> updateQuantity(@PathVariable Long id, @RequestBody CartItemUpdateDTO updateDTO) {
		if (updateDTO == null || updateDTO.getQuantity() == null) throw new BadRequestException("Cantidad requerida para actualizar");
		CartItemDTO updated = cartItemService.updateQuantity(id, updateDTO.getQuantity());
		if (updated == null) throw new ResourceNotFoundException("Item del carrito no encontrado: id=" + id);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> removeItem(@PathVariable Long id) {
		cartItemService.removeCartItem(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
		cartItemService.clearCart(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/price-mismatch")
	public ResponseEntity<java.util.List<CartItemDTO>> getPriceMismatchAll() {
		java.util.List<CartItemDTO> items = cartItemService.findCartItemsWithPriceMismatch(null);
		return ResponseEntity.ok(items);
	}

	@GetMapping("/price-mismatch/{userId}")
	public ResponseEntity<java.util.List<CartItemDTO>> getPriceMismatchByUser(@PathVariable Long userId) {
		java.util.List<CartItemDTO> items = cartItemService.findCartItemsWithPriceMismatch(userId);
		return ResponseEntity.ok(items);
	}

}

