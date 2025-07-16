package com.flipkartclone.ecommercebackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.flipkartclone.ecommercebackend.model.User;
import com.flipkartclone.ecommercebackend.repository.OrderRepository;
import com.flipkartclone.ecommercebackend.repository.UserRepository;
import com.flipkartclone.ecommercebackend.dto.PaymentOrderRequest;
import com.flipkartclone.ecommercebackend.dto.PaymentOrderResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Still needed if you keep using @Value for other properties
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import com.flipkartclone.ecommercebackend.RazorpayConfig; // Import the custom config class

@Service
public class PaymentService {

    // Removed @Value annotations, keys are now loaded from RazorpayConfig
    // @Value("${razorpay.key.id}")
    // private String razorpayKeyId;
    // @Value("${razorpay.key.secret}")
    // private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new payment order with Razorpay.
     * @param request DTO containing order details.
     * @return Razorpay Order object.
     * @throws RuntimeException if Razorpay API call fails, or internal order/user not found.
     */
    @Transactional
    public PaymentOrderResponse createRazorpayOrder(PaymentOrderRequest request) {
        try {
            // Use keys from RazorpayConfig
            RazorpayClient razorpay = new RazorpayClient(RazorpayConfig.RAZORPAY_KEY_ID, RazorpayConfig.RAZORPAY_KEY_SECRET);

            com.flipkartclone.ecommercebackend.model.Order internalOrder = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Internal Order not found with ID: " + request.getOrderId()));

            userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            if (internalOrder.getTotalAmount().compareTo(request.getAmount()) != 0) {
                throw new RuntimeException("Amount mismatch for order ID: " + request.getOrderId());
            }

            long amountInSmallestUnit = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            if (amountInSmallestUnit < 0) throw new RuntimeException("Amount cannot be negative.");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getReceipt());

            JSONObject notes = new JSONObject();
            notes.put("internal_order_id", request.getOrderId().toString());
            Optional<User> userOptional = userRepository.findById(request.getUserId());
            if (userOptional.isPresent()) {
                notes.put("user_email", userOptional.get().getEmail());
            } else {
                notes.put("user_email", "unknown_user@example.com");
            }
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpay.orders.create(orderRequest);

            PaymentOrderResponse response = new PaymentOrderResponse();
            response.setId(razorpayOrder.get("id"));
            response.setEntity(razorpayOrder.get("entity"));
            response.setAmount(razorpayOrder.get("amount"));
            response.setAmount_paid(razorpayOrder.get("amount_paid"));
            response.setAmount_due(razorpayOrder.get("amount_due"));
            response.setCurrency(razorpayOrder.get("currency"));
            response.setReceipt(razorpayOrder.get("receipt"));
            response.setStatus(razorpayOrder.get("status"));
            response.setAttempts(razorpayOrder.get("attempts")); // <-- This line was cut off

            Object createdAtObj = razorpayOrder.get("created_at");
            if (createdAtObj instanceof Number) {
                response.setCreated_at(((Number) createdAtObj).longValue());
            } else if (createdAtObj instanceof Date) {
                response.setCreated_at(((Date) createdAtObj).getTime() / 1000);
            } else {
                throw new RuntimeException("Unexpected type for created_at from Razorpay: " + createdAtObj.getClass().getName());
            }

            return response;

        } catch (RazorpayException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during payment order creation: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a Razorpay webhook signature and processes payment status update.
     * @param payload The raw JSON payload from Razorpay webhook request.
     * @param signature The 'X-Razorpay-Signature' header value.
     * @return True if signature is valid and payment status is processed, false otherwise.
     * @throws RuntimeException if signature verification fails or internal order not found.
     */
    @Transactional
    public boolean handleWebhook(String payload, String signature) {
        try {
            // Use secret from RazorpayConfig
            boolean isValidSignature = Utils.verifyWebhookSignature(payload, signature, RazorpayConfig.RAZORPAY_KEY_SECRET);
            if (!isValidSignature) {
                throw new RuntimeException("Webhook signature verification failed.");
            }

            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            JSONObject paymentEntity = webhookData.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String razorpayOrderId = paymentEntity.getString("order_id");
            String paymentStatus = paymentEntity.getString("status");
            String internalReceiptId = paymentEntity.getString("receipt");

            Long internalOrderId = Long.parseLong(internalReceiptId.replace("order_receipt_", ""));

            com.flipkartclone.ecommercebackend.model.Order internalOrder = orderRepository.findById(internalOrderId)
                    .orElseThrow(() -> new RuntimeException("Internal Order not found for Razorpay order ID: " + razorpayOrderId));

            if ("captured".equals(paymentStatus)) {
                internalOrder.setStatus("PAID");
            } else if ("failed".equals(paymentStatus)) {
                internalOrder.setStatus("FAILED");
            } else if ("order.paid".equals(event)) {
                internalOrder.setStatus("PAID");
            }

            orderRepository.save(internalOrder);

            System.out.println("Webhook processed: Order " + internalOrder.getId() + " updated to " + internalOrder.getStatus());
            return true;

        } catch (RazorpayException e) {
            e.printStackTrace();
            throw new RuntimeException("Razorpay Webhook processing failed: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during webhook processing: " + e.getMessage(), e);
        }
    }
}