package kz.pandev.bot.telegrambotapplication.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель узла дерева категорий.
 * Используется для представления категорий в иерархической структуре.
 */
@Data
public class TreeNode {
    private String name;
    private List<TreeNode> children = new ArrayList<>(); // Список потомков (вложенные узлы)

    // Конструктор нужен, чтобы сразу задать имя узла при создании
    public TreeNode(String name) {
        this.name = name;
    }
}
