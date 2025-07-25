package com.flipkartclone.ecommercebackend.repository;

import com.flipkartclone.ecommercebackend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(Long orderId);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(Long userId);
}