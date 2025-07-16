package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.model.product.Product;
import com.flipkartclone.ecommercebackend.service.ProductService;
import com.flipkartclone.ecommercebackend.dto.ProductRequest;
import com.flipkartclone.ecommercebackend.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Helper method to convert Product entity to ProductResponse DTO
    private ProductResponse convertToDto(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    /**
     * Endpoint to add a new product using ProductRequest DTO.
     * @param request The ProductRequest DTO sent in the request body.
     * @return ResponseEntity with the added ProductResponse and HTTP status 201 (Created)
     * or 409 (Conflict) if a product with the same name exists, or 400 if category not found.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request) {
        try {
            Product newProduct = productService.addProduct(request);
            return new ResponseEntity<>(convertToDto(newProduct), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Category not found")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }

    /**
     * Endpoint to get all products.
     * @return ResponseEntity with a list of all ProductResponse DTOs and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        // CORRECTED: Call getAllProducts() from productService
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> dtos = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    /**
     * Endpoint to get a product by its ID.
     * @param id The ID of the product.
     * @return ResponseEntity with the ProductResponse DTO and HTTP status 200 (OK) or 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<Product> productOptional = productService.getProductById(id);
        return productOptional.map(product -> new ResponseEntity<>(convertToDto(product), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to update an existing product using ProductRequest DTO.
     * @param id The ID of the product to update.
     * @param request The ProductRequest DTO with updated details and categoryId.
     * @return ResponseEntity with the updated ProductResponse DTO and HTTP status 200 (OK)
     * or 404 (Not Found) or 400 if category not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            Optional<Product> updatedProductOptional = productService.updateProduct(id, request);
            return updatedProductOptional.map(updatedProduct -> new ResponseEntity<>(convertToDto(updatedProduct), HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Category not found")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            throw e;
        }
    }

    /**
     * Endpoint to delete a product by its ID.
     * @param id The ID of the product to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on success
     * or 404 (Not Found) if product does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}