package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloWorldController {

    @Autowired
    private EmailService emailService; // Inject the EmailService

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Flipkart Clone Backend!";
    }

    // This is the new endpoint for testing
    @GetMapping("/test-email")
    public String testEmail() {
        // IMPORTANT: Change this to a real email address you can check
        emailService.sendSimpleMessage(
                "storyhumm@gmail.com",
                "Test Email from Flipkart Clone",
                "This is a test email to confirm the configuration is working."
        );
        return "Test email sent!";
    }
}