package kz.pandev.bot.telegrambotapplication.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotCommand {

    String getCommand();

    void execute(Update update, TelegramLongPollingBot bot);
}
