package com.sagafitmi.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.sagafitmi.ecommerce.model.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	// find items by user id
	List<CartItem> findByUserId(Long userId);

	// find existing cart item for a user and product
	Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

	// delete all items for a user
	void deleteByUserId(Long userId);

	// existe al menos un cart item para un producto
	boolean existsByProductId(Long productId);

	// existe al menos un cart item para un usuario
	boolean existsByUserId(Long userId);
}
