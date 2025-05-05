package kz.pandev.bot.telegrambotapplication.bot;

import kz.pandev.bot.telegrambotapplication.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    public Bot (BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Здесь логика обработки входящих сообщений
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Пример ответа
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Вы написали: " + text);

            try {
                execute(message); // метод из TelegramLongPollingBot
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}
