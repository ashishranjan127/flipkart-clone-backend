package com.flipkartclone.ecommercebackend.service;

import com.flipkartclone.ecommercebackend.model.Category;
import com.flipkartclone.ecommercebackend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Adds a new category to the database.
     * @param category The category object to save.
     * @return The saved Category object.
     * @throws RuntimeException if a category with the given name already exists.
     */
    public Category addCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category with this name already exists.");
        }
        return categoryRepository.save(category);
    }

    /**
     * Retrieves a category by its ID.
     * @param id The ID of the category.
     * @return An Optional containing the Category if found, or empty otherwise.
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Retrieves all categories.
     * @return A list of all categories.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Updates an existing category.
     * @param id The ID of the category to update.
     * @param updatedCategory The category object with updated details.
     * @return An Optional containing the updated Category if found, or empty otherwise.
     */
    public Optional<Category> updateCategory(Long id, Category updatedCategory) {
        Optional<Category> existingCategoryOptional = categoryRepository.findById(id);

        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();
            existingCategory.setName(updatedCategory.getName());
            existingCategory.setDescription(updatedCategory.getDescription());

            return Optional.of(categoryRepository.save(existingCategory));
        }
        return Optional.empty();
    }

    /**
     * Deletes a category by its ID.
     * @param id The ID of the category to delete.
     * @return True if the category was found and deleted, false otherwise.
     */
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}