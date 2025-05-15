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

    /**
     * Проверяет наличие категории с указанным именем.
     *
     * @param categoryName имя категории
     * @return true, если категория существует; иначе false
     */
    public boolean existsByName(String categoryName) {
        return categoryRepository.findByName(categoryName).isPresent();
    }

    /**
     * Создаёт новую корневую категорию.
     *
     * @param name имя новой категории
     * @return созданная категория
     * @throws IllegalArgumentException если категория с таким именем уже существует
     */
    public Category createRootCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Название категории уже существует");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    /**
     * Создаёт дочернюю категорию с привязкой к родительской.
     *
     * @param parentName имя родительской категории
     * @param childName имя новой дочерней категории
     * @return созданная дочерняя категория
     * @throws IllegalArgumentException если родительская категория не найдена или имя уже занято
     */
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


    /**
     * Ищет категорию по ID и выбрасывает исключение, если не найдена.
     *
     * @param id идентификатор категории
     * @return найденная категория
     * @throws IllegalArgumentException если категория не найдена
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + id + " не найдена"));
    }

    /**
     * Получает категорию по ID или возвращает null, если не найдена.
     *
     * @param id идентификатор категории
     * @return найденная категория или null
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Удаляет категорию по ID.
     *
     * @param id идентификатор категории
     */
    public void deleteCategoryById(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }


    /**
     * Получает список всех категорий.
     *
     * @return список всех категорий
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Получает список всех категорий в виде DTO.
     * Используется для представления категорий в упрощённой форме.
     *
     * @return список DTO категорий
     */
    public List<CategoryDto> getAllCategoryDtos() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            dto.setName(category.getName());
            dto.setParent(category.getParent() == null); // true если это корневая категория
            return dto;
        }).collect(Collectors.toList());
    }


    // Получение Optional по имени
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
}

