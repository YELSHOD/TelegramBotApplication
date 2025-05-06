package kz.pandev.bot.telegrambotapplication.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class HelpCommand implements BotCommand {

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String helpText = """
        Доступные команды:
        /start - Запуск бота
        /help - Справка
        /addElement - Добавить элемент
        /removeElement - Удалить элемент
        /viewTree - Показать дерево категорий
        /viewCategories - Показать список всех категорий
        /download - Скачать Excel документ с деревом категорий
        """;

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(helpText);

        try {
            bot.execute(message);
            log.debug("Команда /help выполнена для чата {}", update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /help для чата {}: {}", update.getMessage().getChatId(), e.getMessage());
        }
    }
}
