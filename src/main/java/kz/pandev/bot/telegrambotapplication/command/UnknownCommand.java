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
public class UnknownCommand implements BotCommand {

    @Override
    public String getCommand() {
        return "unknown";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        }

        if (chatId == null) {
            log.error("Ошибка: не найден chatId в Update");
            return;
        }

        String userMessage = update.hasMessage() ? update.getMessage().getText() : "Callback query received";
        String userName = update.hasMessage() ? update.getMessage().getFrom().getUserName() : "Unknown";

        log.info("Получено сообщение от пользователя: {} | chatId: {} | текст: {}", userName, chatId, userMessage);

        String responseText = """
        ❗ Неизвестная команда.
        Пожалуйста, воспользуйтесь кнопками ниже или введите /help.
        """;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

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
            log.info("Пользователю {} отправлено сообщение с клавиатурой", userName);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}", userName, e);
        }
    }
}

