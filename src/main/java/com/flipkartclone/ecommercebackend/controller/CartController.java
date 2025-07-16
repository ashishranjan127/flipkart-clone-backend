package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.model.Cart; // <-- Ensure this is present
import com.flipkartclone.ecommercebackend.model.CartItem; // <-- Ensure this is present (used by helper method)
import com.flipkartclone.ecommercebackend.service.CartService; // <-- Ensure this is present
import com.flipkartclone.ecommercebackend.dto.CartItemRequest; // <-- Ensure this is present
import com.flipkartclone.ecommercebackend.dto.CartResponse; // <-- Ensure this is present
import com.flipkartclone.ecommercebackend.dto.CartItemResponse; // <-- Ensure this is present

import org.springframework.beans.factory.annotation.Autowired; // <-- Ensure this is present
import org.springframework.http.ResponseEntity; // <-- Ensure this is present
import org.springframework.http.HttpStatus; // <-- Ensure this is present
import org.springframework.web.bind.annotation.*; // <-- Ensure this is present (wildcard import for @RestController, @RequestMapping, etc.)

import java.math.BigDecimal; // <-- Ensure this is present
import java.util.List; // <-- Ensure this is present
import java.util.stream.Collectors; // <-- Ensure this is present

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Helper method to convert CartItem entity to DTO
    private CartItemResponse toDto(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getImageUrl(),
                cartItem.getQuantity(),
                cartItem.getPriceAtPurchase(),
                cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    // Helper method to convert Cart entity to DTO
    private CartResponse toDto(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getItemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUser().getId(), items, total);
    }

    /**
     * Endpoint to get a user's cart. Creates one if it doesn't exist.
     * GET /api/cart/{userId}
     * @param userId The ID of the user.
     * @return The user's CartResponse DTO.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getUserCart(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getUserCart(userId);
            return ResponseEntity.ok(toDto(cart));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 User not found
        }
    }

    /**
     * Endpoint to add a product to cart or update its quantity.
     * POST /api/cart/{userId}/items
     * @param userId The ID of the user.
     * @param request The CartItemRequest DTO (productId, quantity).
     * @return The updated CartResponse DTO.
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addOrUpdateCartItem(@PathVariable Long userId, @RequestBody CartItemRequest request) {
        try {
            cartService.addOrUpdateCartItem(userId, request.getProductId(), request.getQuantity());
            Cart cart = cartService.getUserCart(userId); // Fetch the cart again to get updated state
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(cart)); // 201 Created if added, 200 OK if updated
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 User not found
            }
            if (e.getMessage().contains("Product not found")) {
                return ResponseEntity.badRequest().build(); // 400 Product not found
            }
            if (e.getMessage().contains("Quantity must be positive")) {
                return ResponseEntity.badRequest().build(); // 400 Invalid quantity
            }
            return ResponseEntity.internalServerError().build(); // Generic 500 for other unexpected runtime errors
        }
    }

    /**
     * Endpoint to update the quantity of an existing item in the cart.
     * PUT /api/cart/{userId}/items/{productId}
     * @param userId The ID of the user.
     * @param productId The ID of the product in the cart item.
     * @param newQuantity The new quantity.
     * @return The updated CartResponse DTO.
     */
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer newQuantity) {
        try {
            cartService.updateCartItemQuantity(userId, productId, newQuantity);
            Cart cart = cartService.getUserCart(userId); // Fetch cart again
            return ResponseEntity.ok(toDto(cart));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to remove an item from the user's cart.
     * DELETE /api/cart/{userId}/items/{productId}
     * @param userId The ID of the user.
     * @param productId The ID of the product in the cart item to remove.
     * @return HTTP status 204 (No Content) on success or 404 (Not Found).
     */
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long userId, @PathVariable Long productId) {
        try {
            boolean removed = cartService.removeCartItem(userId, productId);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}