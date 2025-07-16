package com.flipkartclone.ecommercebackend.controller;

import com.flipkartclone.ecommercebackend.model.Category; // <-- CORRECTED IMPORT PATH
import com.flipkartclone.ecommercebackend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Endpoint to add a new category.
     * @param category The Category object sent in the request body.
     * @return ResponseEntity with the added Category and HTTP status 201 (Created)
     * or 409 (Conflict) if a category with the same name exists.
     */
    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        try {
            Category newCategory = categoryService.addCategory(category);
            return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); // Category name already exists
        }
    }

    /**
     * Endpoint to get all categories.
     * @return ResponseEntity with a list of all categories and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    /**
     * Endpoint to get a category by its ID.
     * @param id The ID of the category.
     * @return ResponseEntity with the Category and HTTP status 200 (OK) or 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to update an existing category.
     * @param id The ID of the category to update.
     * @param category The updated Category object.
     * @return ResponseEntity with the updated Category and HTTP status 200 (OK)
     * or 404 (Not Found).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Optional<Category> updated = categoryService.updateCategory(id, category);
        return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to delete a category by its ID.
     * @param id The ID of the category to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on success
     * or 404 (Not Found) if category does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}