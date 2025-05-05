package kz.pandev.bot.telegrambotapplication.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand implements BotCommand {


    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();
        SendMessage message = new SendMessage(chatId, "Добро пожаловать!");

        try {
            bot.execute(message);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
