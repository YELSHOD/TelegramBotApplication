package kz.pandev.bot.telegrambotapplication.util;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Утилитный компонент для построения иерархического дерева категорий.
 * Используется для представления категорий в структурированной древовидной форме.
 */
@Component
@RequiredArgsConstructor
public class TreeBuilder {

    private final CategoryRepository categoryRepository;

    /**
     * Строит полное дерево категорий начиная с заданного родительского ID.
     * Обычно вызывается с {@code null}, чтобы получить все корневые категории.
     *
     * @param parentId ID родительской категории (или {@code null} для корня дерева)
     * @return корневой узел дерева, содержащий все дочерние категории в виде подузлов
     */
    public TreeNode buildTree(Long parentId) {
        List<Category> rootCategories = categoryRepository.findByParentId(parentId)
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();

        // Корневой узел дерева (заголовок, не привязан к сущности Category)
        TreeNode root = new TreeNode("Категории в структурированном виде дерева \n");

        for (Category category : rootCategories) {
            TreeNode childNode = buildSubTree(category);
            root.getChildren().add(childNode);
        }

        return root;
    }

    /**
     * Рекурсивно строит поддерево для переданной категории, включая всех её потомков.
     *
     * @param category категория, для которой строится поддерево
     * @return узел дерева, соответствующий переданной категории и содержащий вложенные подкатегории
     */
    private TreeNode buildSubTree(Category category) {
        TreeNode node = new TreeNode(category.getName());

        List<Category> children = categoryRepository.findByParentId(category.getId())
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();

        for (Category child : children) {
            node.getChildren().add(buildSubTree(child));
        }

        return node;
    }
}
