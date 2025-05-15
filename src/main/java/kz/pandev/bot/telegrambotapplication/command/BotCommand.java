package kz.pandev.bot.telegrambotapplication.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public interface BotCommand {

    /**
     * Возвращает команду, которую реализует этот обработчик, например "/addelement".
     *
     * @return команда в виде строки
     */
    String getCommand();

    /**
     * Выполняет логику обработки команды.
     *
     * @param update объект обновления от Telegram, содержащий данные о сообщении и событии
     * @param bot экземпляр TelegramLongPollingBot для взаимодействия с Telegram API
     */
    void execute(Update update, TelegramLongPollingBot bot);
}
