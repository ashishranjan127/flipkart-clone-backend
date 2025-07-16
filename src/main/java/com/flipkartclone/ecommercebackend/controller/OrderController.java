package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.model.Order;
import com.flipkartclone.ecommercebackend.model.OrderItem;
import com.flipkartclone.ecommercebackend.service.OrderService; // Import OrderService
import com.flipkartclone.ecommercebackend.dto.OrderRequest; // Import OrderRequest DTO
import com.flipkartclone.ecommercebackend.dto.OrderResponse; // Import OrderResponse DTO
import com.flipkartclone.ecommercebackend.dto.OrderItemResponse; // Import OrderItemResponse DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders") // Base path for order operations
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Helper method to convert OrderItem entity to OrderItemResponse DTO
    private OrderItemResponse convertOrderItemToDto(OrderItem orderItem) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtOrder(orderItem.getPriceAtOrder());
        dto.setItemTotalPrice(orderItem.getPriceAtOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }

    // Helper method to convert Order entity to OrderResponse DTO
    private OrderResponse convertOrderToDto(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());

        List<OrderItemResponse> itemDtos = order.getOrderItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
        dto.setOrderItems(itemDtos);
        return dto;
    }

    /**
     * Endpoint to place a new order from a user's cart.
     * POST /api/orders
     * @param request The OrderRequest DTO (userId, shippingAddress).
     * @return The created OrderResponse DTO.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        try {
            Order newOrder = orderService.placeOrder(request.getUserId(), request.getShippingAddress());
            return new ResponseEntity<>(convertOrderToDto(newOrder), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // More granular error handling for user not found, empty cart, etc.
            if (e.getMessage().contains("User not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
            }
            if (e.getMessage().contains("Cart not found") || e.getMessage().contains("empty cart")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
            }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Generic 500
        }
    }

    /**
     * Endpoint to get all orders for a specific user.
     * GET /api/orders/user/{userId}
     * @param userId The ID of the user.
     * @return A list of OrderResponse DTOs.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            List<OrderResponse> dtos = orders.stream()
                    .map(this::convertOrderToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
            }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Generic 500
        }
    }

    /**
     * Endpoint to get a single order by its ID.
     * GET /api/orders/{orderId}
     * @param orderId The ID of the order.
     * @return An OrderResponse DTO.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        Optional<Order> orderOptional = orderService.getOrderById(orderId);
        return orderOptional.map(order -> new ResponseEntity<>(convertOrderToDto(order), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // We will add more endpoints later for updating order status, etc.
}