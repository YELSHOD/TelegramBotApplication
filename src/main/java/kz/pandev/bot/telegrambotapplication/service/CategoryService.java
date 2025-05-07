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

    // Проверка, существует ли категория с таким именем
    public boolean existsByName(String categoryName) {
        return categoryRepository.findByName(categoryName).isPresent();
    }

    // Создание корневой категории (без родителя)
    public Category createRootCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Название категории уже существует");
        }
        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    // Создание дочерней категории — ищем родителя по имени и привязываем
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

    // Удаление категории по имени — сначала ищем, потом удаляем
    public void deleteCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        categoryRepository.delete(category);
    }

    // Получение всех категорий — используется при построении дерева
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<CategoryDto> getAllCategoryDtos() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            dto.setName(category.getName());
            dto.setParent(category.getParent() == null); // Родитель — если нет родителя
            return dto;
        }).collect(Collectors.toList());
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    // Поиск категории по имени — возвращается Optional
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
}
