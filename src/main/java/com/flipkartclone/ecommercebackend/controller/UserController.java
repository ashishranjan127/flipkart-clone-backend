package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.model.User; // Import the User entity
import com.flipkartclone.ecommercebackend.service.UserService; // Import the UserService
import com.flipkartclone.ecommercebackend.dto.LoginRequest; // Import the LoginRequest DTO
import org.springframework.beans.factory.annotation.Autowired; // For dependency injection
import org.springframework.http.HttpStatus; // For HTTP status codes
import org.springframework.http.ResponseEntity; // For building HTTP responses
import org.springframework.web.bind.annotation.*; // For REST annotations like @RestController, @PostMapping, @RequestBody, @GetMapping, @PathVariable

import java.util.Optional; // Needed for Optional return types

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/users") // Base path for all user-related endpoints
public class UserController {

    private final UserService userService; // Declare an instance of UserService

    // Constructor for dependency injection of UserService
    @Autowired // Spring will automatically inject UserService here
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to register a new user.
     *
     * @param user The User object sent in the request body.
     * @return ResponseEntity with the registered User and HTTP status 201 (Created)
     * or HTTP status 409 (Conflict) if email already exists.
     */
    @PostMapping("/register") // Maps HTTP POST requests to /api/users/register
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            // Return 201 Created status with the registered user object
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // If email already exists, return 409 Conflict status
            // In a real app, you might return a custom error DTO here
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }

    /**
     * Endpoint to authenticate a user (login).
     *
     * @param loginRequest DTO containing email and password.
     * @return ResponseEntity with the authenticated User (or a token in real app)
     * and HTTP status 200 (OK) or 401 (Unauthorized).
     */
    @PostMapping("/login") // Maps HTTP POST requests to /api/users/login
    public ResponseEntity<User> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> authenticatedUser = userService.authenticateUser(
                loginRequest.getEmail(), loginRequest.getPassword()
        );

        if (authenticatedUser.isPresent()) {
            // In a real application, you would generate and return a JWT token here
            return new ResponseEntity<>(authenticatedUser.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }
    }

    /**
     * Endpoint to get a user's profile by ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity with the User object and HTTP status 200 (OK)
     * or 404 (Not Found).
     */
    @GetMapping("/{id}") // Maps HTTP GET requests to /api/users/{id}
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);

        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // We will add more endpoints here later for profile update, etc.
}