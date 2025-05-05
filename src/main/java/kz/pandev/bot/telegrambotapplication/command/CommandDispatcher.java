package kz.pandev.bot.telegrambotapplication.command;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class CommandDispatcher {

    private final Map<String, BotCommand> commandMap = new HashMap<>();

    public CommandDispatcher() {
        register(new StartCommand());
    }

    private void register(BotCommand command) {
        commandMap.put(command.getCommand(), command);
    }



    public void dispatch(Update update, TelegramLongPollingBot bot) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            BotCommand command = commandMap.get(text.split(" ")[0]);
            if (command != null) {
                command.execute(update, bot);
            }
        }
    }
}
