package kz.pandev.bot.telegrambotapplication.repository;

import kz.pandev.bot.telegrambotapplication.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentId(Long parentId);

    boolean existsByName(String name);

}
