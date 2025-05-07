package kz.pandev.bot.telegrambotapplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


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

    // Ссылка на родительскую категорию (для построения иерархии)
    @ManyToOne
    private Category parent;

    // Список дочерних категорий — связь один-ко-многим.
    // mappedBy = "parent" указывает, что владеющая сторона — поле parent
    // CascadeType.ALL — каскадируем все операции (persist, merge, remove и т.д.)
    // orphanRemoval = true — если убираем дочерний элемент из списка, он удаляется из БД
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();


    // Метод для проверки, является ли категория родительской
    public boolean isParent() {
        return parent == null && !children.isEmpty(); // Если нет родителя и есть дочерние категории
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }
}
