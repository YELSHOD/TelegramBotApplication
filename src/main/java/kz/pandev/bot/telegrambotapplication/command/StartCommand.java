package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.util.KeyboardFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Component
public class StartCommand implements BotCommand {

    /**
     * Возвращает название команды.
     */
    @Override
    public String getCommand() {
        return "/start";
    }

    /**
     * Выполняет команду: отправляет сообщение с основным меню клавиатуры.
     *
     * @param update Объект Update от Telegram API
     * @param bot    Экземпляр TelegramLongPollingBot для отправки сообщений
     */
    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();
        SendMessage message = new SendMessage(chatId, "Добро пожаловать! Используйте кнопки ниже для работы с ботом.");
        message.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());

        try {
            bot.execute(message);
            log.debug("Команда /start успешно выполнена для чата {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка при выполнении команды /start для чата {}: {}", chatId, e.getMessage());
        }
    }
}
