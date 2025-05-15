package kz.pandev.bot.telegrambotapplication.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    private KeyboardFactory() {
        // закрываем конструктор
    }

    public static ReplyKeyboardMarkup mainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📘 Справка");
        row1.add("➕ Добавить элемент");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🌳 Дерево категорий");
        row2.add("➖ Удалить элемент");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("👁 Просмотр категорий");
        row3.add("📊 Импорт Excel");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("📥 Скачать Excel");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
