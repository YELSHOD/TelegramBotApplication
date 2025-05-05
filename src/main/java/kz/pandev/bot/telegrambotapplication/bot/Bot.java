package kz.pandev.bot.telegrambotapplication.bot;

import kz.pandev.bot.telegrambotapplication.command.CommandDispatcher;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Bot extends TelegramLongPollingBot {

    private final CommandDispatcher dispatcher;

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

    @Override
    public void onUpdateReceived(Update update) {
        dispatcher.dispatch(update, this);
    }
}
