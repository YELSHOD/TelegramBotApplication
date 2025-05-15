package kz.pandev.bot.telegrambotapplication.repository;

import kz.pandev.bot.telegrambotapplication.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Category}.
 * Используется Spring Data JPA для доступа к данным о категориях и их иерархии.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Ищет категорию по точному имени.
     *
     * @param name имя категории
     * @return {@link Optional}, содержащий найденную категорию или пустой, если не найдено
     */
    Optional<Category> findByName(String name);

    /**
     * Находит все подкатегории по ID родительской категории.
     *
     * @param parentId ID родительской категории
     * @return список дочерних категорий
     */
    List<Category> findByParentId(Long parentId);

    /**
     * Проверяет, существует ли категория с указанным именем.
     *
     * @param name имя категории
     * @return true, если категория существует; false — если нет
     */
    boolean existsByName(String name);

}
