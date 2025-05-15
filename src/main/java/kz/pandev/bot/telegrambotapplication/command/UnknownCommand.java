package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.util.KeyboardFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработчик неизвестной команды Telegram-бота.
 * <p>
 * Выполняется, когда пользователь отправляет сообщение, не соответствующее ни одной зарегистрированной команде.
 * Отправляет стандартный ответ с клавиатурой основного меню.
 */
@Slf4j
@Component
public class UnknownCommand implements BotCommand {

    /**
     * Возвращает имя команды. В данном случае это маркер "unknown", используемый для обработки неподдерживаемых команд.
     *
     * @return строка "unknown"
     */
    @Override
    public String getCommand() {
        return "unknown";
    }

    /**
     * Выполняет обработку неизвестной команды:
     * <ul>
     *     <li>Определяет chatId из сообщения или callback-запроса</li>
     *     <li>Логирует полученное сообщение</li>
     *     <li>Отправляет ответ с клавиатурой основного меню</li>
     * </ul>
     *
     * @param update объект {@link Update} от Telegram API, содержащий входящее сообщение или callback
     * @param bot    экземпляр {@link TelegramLongPollingBot} для отправки ответа
     */
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

        // Устанавливаем основное меню клавиатуры
        message.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());

        try {
            bot.execute(message);
            log.info("Пользователю {} отправлено сообщение с клавиатурой", userName);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}", userName, e);
        }
    }
}


