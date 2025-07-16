package com.flipkartclone.ecommercebackend.service;

import com.flipkartclone.ecommercebackend.model.Cart;
import com.flipkartclone.ecommercebackend.model.CartItem;
import com.flipkartclone.ecommercebackend.model.Order;
import com.flipkartclone.ecommercebackend.model.OrderItem;
import com.flipkartclone.ecommercebackend.model.User;
import com.flipkartclone.ecommercebackend.model.product.Product;
import com.flipkartclone.ecommercebackend.repository.CartItemRepository;
import com.flipkartclone.ecommercebackend.repository.CartRepository;
import com.flipkartclone.ecommercebackend.repository.ProductRepository;
import com.flipkartclone.ecommercebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getUserCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Optional<Cart> existingCartWithItems = cartRepository.findByUserIdWithItems(userId);

        return existingCartWithItems
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public CartItem addOrUpdateCartItem(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive.");
        }

        Cart cart = getUserCart(userId);

        Product product = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtPurchase(product.getPrice());
            cart.getCartItems().add(cartItem);
        }

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public Optional<CartItem> updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new RuntimeException("New quantity must be positive. Use removeCartItem to delete.");
        }

        Cart cart = getUserCart(userId);

        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.setQuantity(newQuantity);
            return Optional.of(cartItemRepository.save(cartItem));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean removeCartItem(Long userId, Long productId) {
        Cart cart = getUserCart(userId);

        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            return true;
        }
        return false;
    }
}