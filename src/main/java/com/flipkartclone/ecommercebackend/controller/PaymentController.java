package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.dto.PaymentOrderRequest;
import com.flipkartclone.ecommercebackend.dto.PaymentOrderResponse;
import com.flipkartclone.ecommercebackend.dto.RazorpayWebhookRequest; // <-- NEW IMPORT for Webhook DTO
import com.flipkartclone.ecommercebackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint to create a Razorpay order for a payment.
     * POST /api/payment/create-order
     * @param request DTO containing internal order details, amount, etc.
     * @return Razorpay Order details.
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(@RequestBody PaymentOrderRequest request) {
        try {
            PaymentOrderResponse razorpayOrder = paymentService.createRazorpayOrder(request);
            return new ResponseEntity<>(razorpayOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Order not found") || e.getMessage().contains("User not found") || e.getMessage().contains("Amount mismatch")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to receive webhooks from Razorpay for payment status updates.
     * POST /api/payment/webhook
     * @param rawPayload The raw request body as a string.
     * @param signature The 'X-Razorpay-Signature' header.
     * @return HTTP 200 OK if processed, 400 Bad Request if signature invalid/error.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String rawPayload, // Capture raw payload for signature verification
            @RequestHeader("X-Razorpay-Signature") String signature) { // Capture signature header
        try {
            // PaymentService handles signature verification and status update
            boolean processed = paymentService.handleWebhook(rawPayload, signature);
            if (processed) {
                return new ResponseEntity<>("Webhook processed successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Webhook processing failed", HttpStatus.BAD_REQUEST);
            }
        } catch (RuntimeException e) {
            e.printStackTrace(); // Print full stack trace to console for debugging
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Return error message
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred during webhook processing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}