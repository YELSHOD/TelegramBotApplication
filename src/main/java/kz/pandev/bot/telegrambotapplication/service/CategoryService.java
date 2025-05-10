package kz.pandev.bot.telegrambotapplication.service;

import kz.pandev.bot.telegrambotapplication.dto.CategoryDto;
import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Проверка существования категории по имени
    public boolean existsByName(String categoryName) {
        return categoryRepository.findByName(categoryName).isPresent();
    }

    // Создание корневой категории
    public Category createRootCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Название категории уже существует");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    // Создание дочерней категории
    public Category createChildCategory(String parentName, String childName) {
        Category parent = categoryRepository.findByName(parentName)
                .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));

        if (categoryRepository.existsByName(childName)) {
            throw new IllegalArgumentException("Название дочерней категории уже существует");
        }

        Category child = new Category();
        child.setName(childName);
        child.setParent(parent);
        return categoryRepository.save(child);
    }

    // Удаление категории по имени
    public void deleteCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        categoryRepository.delete(category);
    }

    // Метод, выбрасывающий исключение при отсутствии
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + id + " не найдена"));
    }

    // Метод, возвращающий null при отсутствии (удобен для безопасной проверки)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    // Удаление категории по ID
    public void deleteCategoryById(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    // Получение категории по имени
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    // Получение всех категорий
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Получение всех категорий в виде DTO
    public List<CategoryDto> getAllCategoryDtos() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            dto.setName(category.getName());
            dto.setParent(category.getParent() == null); // true если это корневая категория
            return dto;
        }).collect(Collectors.toList());
    }

    // Сохранение категории
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    // Получение Optional по имени
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
}

