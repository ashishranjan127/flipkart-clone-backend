package com.flipkartclone.ecommercebackend.service;

import com.flipkartclone.ecommercebackend.model.product.Product;
import com.flipkartclone.ecommercebackend.model.Category;
import com.flipkartclone.ecommercebackend.repository.ProductRepository;
import com.flipkartclone.ecommercebackend.repository.CategoryRepository;
import com.flipkartclone.ecommercebackend.dto.ProductRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product addProduct(ProductRequest request) {
        if (productRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Product with this name already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findByIdWithCategory(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategory();
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, ProductRequest request) {
        Optional<Product> existingProductOptional = productRepository.findByIdWithCategory(id);

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

            existingProduct.setName(request.getName());
            existingProduct.setDescription(request.getDescription());
            existingProduct.setPrice(request.getPrice());
            existingProduct.setStockQuantity(request.getStockQuantity());
            existingProduct.setImageUrl(request.getImageUrl());
            existingProduct.setCategory(category);

            return Optional.of(productRepository.save(existingProduct));
        }
        return Optional.empty();
    }

    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}