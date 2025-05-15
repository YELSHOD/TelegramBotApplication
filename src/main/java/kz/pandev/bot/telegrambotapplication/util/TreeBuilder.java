package kz.pandev.bot.telegrambotapplication.util;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TreeBuilder {

    private final CategoryRepository categoryRepository;

    // Строим дерево начиная с родительского ID (null — корень)
    public TreeNode buildTree(Long parentId) {
        List<Category> rootCategories = categoryRepository.findByParentId(parentId)
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();

        // Корневой узел дерева (не из БД, просто заголовок)
        TreeNode root = new TreeNode("Категории в структурированном виде дерева \n");

        for (Category category : rootCategories) {
            TreeNode childNode = buildSubTree(category);
            root.getChildren().add(childNode);
        }

        return root;
    }

    // Рекурсивное построение поддерева для конкретной категории
    private TreeNode buildSubTree(Category category) {
        TreeNode node = new TreeNode(category.getName());

        List<Category> children = categoryRepository.findByParentId(category.getId())
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();

        // Углубляемся в дерево
        for (Category child : children) {
            node.getChildren().add(buildSubTree(child));
        }

        return node;
    }
}
