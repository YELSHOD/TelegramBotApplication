package kz.pandev.bot.telegrambotapplication.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StartCommand implements BotCommand {

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();
        SendMessage message = new SendMessage(chatId, "Добро пожаловать! Используйте кнопки ниже для работы с ботом.");

        // Клавиатура
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
        message.setReplyMarkup(keyboard);

        try {
            bot.execute(message);
            log.debug("Команда /start успешно выполнена для чата {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка при выполнении команды /start для чата {}: {}", chatId, e.getMessage());
        }
    }
}
