package kz.pandev.bot.telegrambotapplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность категории для построения иерархической структуры.
 * <p>
 * Категории могут иметь родителя и множество дочерних категорий,
 * что позволяет строить дерево категорий.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /**
     * Родительская категория.
     * <p>
     * Если поле равно null — категория считается корневой.
     */
    @ManyToOne
    private Category parent;

    /**
     * Список дочерних категорий.
     * <p>
     * Связь один-ко-многим с каскадированием всех операций
     * обеспечивает каскадное удаление дочерних категорий при удалении родителя
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    /**
     * Проверяет, является ли категория родительской (корневой) —
     * то есть не имеет родителя и содержит дочерние категории.
     *
     * @return true, если категория корневая с потомками, иначе false
     */
    public boolean isParent() {
        return parent == null && !children.isEmpty();
    }


    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }
}
