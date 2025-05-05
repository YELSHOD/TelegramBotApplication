package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public boolean existsByName(String categoryName) {
        return categoryRepository.findByName(categoryName).isPresent();
    }


    public Category createRootCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    public Category createChildCategory(String parentName, String childName) {
        Category parent = categoryRepository.findByName(parentName)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));

        if (categoryRepository.existsByName(childName)) {
            throw new IllegalArgumentException("Child name already exists");
        }

        Category child = new Category();
        child.setName(childName);
        child.setParent(parent);

        return categoryRepository.save(child);
    }

    public void deleteCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        categoryRepository.delete(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
