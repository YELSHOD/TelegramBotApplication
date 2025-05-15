package kz.pandev.bot.telegrambotapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных категории.
 * Содержит имя категории и признак, является ли она родительской.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private String name;
    private boolean isParent;
}
