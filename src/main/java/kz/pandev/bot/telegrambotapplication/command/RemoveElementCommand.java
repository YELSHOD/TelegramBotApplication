package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveElementCommand implements BotCommand {

    private final CategoryService categoryService;

    @Override
    public String getCommand() {
        return "/removeElement";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String[] parts = messageText.split(" ");

        String response;

        if (parts.length == 2) {
            String categoryName = parts[1];
            try {
                categoryService.deleteCategoryByName(categoryName);
                response = "Категория '" + categoryName + "' успешно удалена.";
                log.debug("Категория '{}' удалена для чата {}", categoryName, chatId);
            } catch (IllegalArgumentException e) {
                response = "Ошибка: " + e.getMessage();
                log.error("Ошибка при удалении категории '{}' для чата {}: {}", categoryName, chatId, e.getMessage());
            }
        } else {
            response = "Неправильный формат. Пример: /removeElement НазваниеКатегории";
            log.warn("Неправильный формат команды /removeElement от чата {}", chatId);
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response);

        try {
            bot.execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения для чата {}: {}", chatId, e.getMessage());
        }
    }
}
