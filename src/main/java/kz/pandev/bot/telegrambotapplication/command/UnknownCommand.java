package kz.pandev.bot.telegrambotapplication.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class UnknownCommand implements BotCommand {

    @Override
    public String getCommand() {
        return "unknown";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        String responseText = """
        ❗ Неизвестная команда.
        Попробуйте /help для списка доступных команд.
        """;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

        // Добавим кнопки
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true); // адаптивная высота

        KeyboardRow row = new KeyboardRow();
        row.add("/help");
        row.add("/start");
        row.add("/addElement");
        row.add("/removeElement");
        row.add("/viewTree");
        row.add("/download");

        keyboard.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboard);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
