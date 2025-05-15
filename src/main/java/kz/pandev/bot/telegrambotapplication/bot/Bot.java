package kz.pandev.bot.telegrambotapplication.bot;

import kz.pandev.bot.telegrambotapplication.command.CommandDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Основной Telegram-бот, реализующий логику приёма и обработки обновлений.
 * <p>
 * Получает входящие {@link Update} и делегирует обработку {@link CommandDispatcher}.
 */
@Component
public class Bot extends TelegramLongPollingBot {

    private final CommandDispatcher dispatcher;

    /**
     * Инициализация Telegram-бота с внедрением диспетчера команд.
     *
     * @param dispatcher диспетчер для маршрутизации команд и событий
     */
    public Bot(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.token}")
    private String token;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * Вызывается при каждом входящем обновлении от Telegram.
     *
     * @param update входящее событие (сообщение, callback и т.д.)
     */
    @Override
    public void onUpdateReceived(Update update) {
        dispatcher.dispatch(update, this);
    }
}
