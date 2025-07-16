package com.flipkartclone.ecommercebackend.service;

import com.flipkartclone.ecommercebackend.model.Cart;
import com.flipkartclone.ecommercebackend.model.CartItem;
import com.flipkartclone.ecommercebackend.model.Order;
import com.flipkartclone.ecommercebackend.model.OrderItem;
import com.flipkartclone.ecommercebackend.model.User;
import com.flipkartclone.ecommercebackend.model.product.Product;
import com.flipkartclone.ecommercebackend.repository.CartRepository;
import com.flipkartclone.ecommercebackend.repository.OrderRepository;
import com.flipkartclone.ecommercebackend.repository.OrderItemRepository;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        UserRepository userRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Places a new order for a user based on their current cart.
     * @param userId The ID of the user placing the order.
     * @param shippingAddress The address for shipping.
     * @return The created Order entity.
     * @throws RuntimeException if user not found, cart is empty, or cart not found.
     */
    @Transactional
    public Order placeOrder(Long userId, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with an empty cart.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(cartItem.getPriceAtPurchase());
            orderItems.add(orderItem);

            totalOrderAmount = totalOrderAmount.add(orderItem.getPriceAtOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalOrderAmount);

        Order savedOrder = orderRepository.save(order);

        cartRepository.delete(cart);

        return savedOrder;
    }

    /**
     * Retrieves all orders for a specific user.
     * @param userId The ID of the user.
     * @return A list of orders for the user.
     */
    public List<Order> getOrdersByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return orderRepository.findByUserIdWithItems(userId);
    }

    /**
     * Retrieves a single order by its ID.
     * @param orderId The ID of the order.
     * @return An Optional containing the Order if found, or empty otherwise.
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findByIdWithItems(orderId);
    }
}