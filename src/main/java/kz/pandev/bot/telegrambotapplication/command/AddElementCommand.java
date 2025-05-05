package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.repository.CategoryRepository;
import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.generics.TelegramBot;

@Component
@RequiredArgsConstructor
public class AddElementCommand implements BotCommand {

    private final AbsSender sender;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Override
    public String getCommand() {
        return "/addElement";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String[] parts = messageText.split(" ");

        try {
            String response;

            if (parts.length == 2) {
                categoryService.createRootCategory(parts[1]);
                response = "Корневая категория добавлена.";
            } else if (parts.length == 3) {
                categoryService.createChildCategory(parts[1], parts[2]);
                response = "Дочерняя категория добавлена.";
            } else {
                response = "Неправильный формат. Пример:\n/addElement Родитель Дочерний";
            }

            sender.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());

        } catch (Exception e) {
            try {
                sender.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Ошибка: " + e.getMessage())
                        .build());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
