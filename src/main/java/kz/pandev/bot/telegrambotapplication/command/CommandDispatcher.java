package kz.pandev.bot.telegrambotapplication.command;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandDispatcher {

    private final Map<String, BotCommand> commandMap = new HashMap<>();
    private final List<BotCommand> commands; // Список команд, автоматически внедряется Spring

    // Конструктор с внедрением команд через Spring
    public CommandDispatcher(List<BotCommand> commands) {
        this.commands = commands;
    }

    // Инициализация команды при запуске (регистрация команд)
    @PostConstruct
    private void init() {
        // Регистрируем все команды из контекста Spring
        for (BotCommand command : commands) {
            commandMap.put(command.getCommand(), command);
        }
    }

    public void dispatch(Update update, TelegramLongPollingBot bot) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            BotCommand command = commandMap.get(text.split(" ")[0]); // Получаем команду по её имени
            if (command != null) {
                command.execute(update, bot); // Выполняем команду
            } else {
                // Если команда не найдена — вызываем команду по умолчанию (UnknownCommand)
                BotCommand unknownCommand = commandMap.get("unknown");
                if (unknownCommand != null) {
                    unknownCommand.execute(update, bot);
                }
            }
        }
    }
}
