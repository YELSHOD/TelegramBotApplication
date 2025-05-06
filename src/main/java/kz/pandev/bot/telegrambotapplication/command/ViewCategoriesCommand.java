package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCategoriesCommand implements BotCommand {

    private final CategoryService categoryService;

    @Override
    public String getCommand() {
        return "/viewCategories";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        try {
            String response = categoryService.getAllCategories().stream()
                    .filter(category -> category.getParent() == null)  // Только корневые
                    .map(Category::getName)
                    .collect(Collectors.joining("\n"));

            if (response.isEmpty()) {
                response = "Корневые категории не найдены.";
            }

            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());

            log.info("Команда /viewCategories успешно выполнена для чата {}", chatId);

        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /viewCategories для чата {}: {}", chatId, e.getMessage());
            try {
                bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Ошибка: " + e.getMessage())
                        .build());
            } catch (Exception ex) {
                log.error("Ошибка при отправке сообщения об ошибке для чата {}: {}", chatId, ex.getMessage());
            }
        }
    }

}

