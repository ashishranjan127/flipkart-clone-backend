package com.flipkartclone.ecommercebackend; // Ensure this matches your base package

import io.github.cdimascio.dotenv.Dotenv; // <-- This import is crucial

public class RazorpayConfig {
    // Load .env variables. It searches for .env in the current working directory.
    // For Spring Boot applications, the root of the project is usually the working directory.
    private static final Dotenv dotenv = Dotenv.load();

    public static final String RAZORPAY_KEY_ID = dotenv.get("RAZORPAY_KEY_ID");
    public static final String RAZORPAY_KEY_SECRET = dotenv.get("RAZORPAY_KEY_SECRET");
}