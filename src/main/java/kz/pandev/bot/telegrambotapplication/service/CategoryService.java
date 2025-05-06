package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
            throw new IllegalArgumentException("Название категории уже существует");
        }
        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    public Category createChildCategory(String parentName, String childName) {
        Category parent = categoryRepository.findByName(parentName)
                .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));

        if (categoryRepository.existsByName(childName)) {
            throw new IllegalArgumentException("Название дочерной категории уже существует");
        }

        Category child = new Category();
        child.setName(childName);
        child.setParent(parent);

        return categoryRepository.save(child);
    }

    public void deleteCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        categoryRepository.delete(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
}
