package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddElementCommand implements BotCommand {

    private final CategoryService categoryService;

    @Override
    public String getCommand() {
        return "/addElement";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String[] parts = messageText.split(" "); // Разделяем на части команду и аргументы

        try {
            String response;

            // Если только одно слово после команды, то добавляем корневую категорию
            if (parts.length == 2) {
                categoryService.createRootCategory(parts[1]);
                response = "Корневая категория '" + parts[1] + "' добавлена.";
                log.debug("Корневая категория '{}' добавлена.", parts[1]);

                // Если два слова, то добавляем дочернюю категорию
            } else if (parts.length == 3) {
                String parentName = parts[1];
                String childName = parts[2];

                // Проверяем, существует ли родительская категория
                if (categoryService.existsByName(parentName)) {
                    categoryService.createChildCategory(parentName, childName);
                    response = "Дочерняя категория '" + childName + "' для родительской '" + parentName + "' добавлена.";
                    log.debug("Дочерняя категория '{}' для родительской '{}' добавлена.", childName, parentName);
                } else {
                    response = "Родительская категория '" + parentName + "' не найдена.";
                    log.warn("Родительская категория '{}' не найдена.", parentName);
                }

            } else {
                response = "Неправильный формат. Пример:\n/addElement {Родитель}\n/addElement {Родитель} {Дочерний}";
                log.warn("Неправильный формат команды /addElement от чата {}", chatId);
            }

            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());

        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /addElement для чата {}: {}", chatId, e.getMessage());
            try {
                bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Ошибка: " + e.getMessage())
                        .build());
            } catch (Exception ex) {
                log.error("Ошибка при отправке сообщения об ошибке для чата {}: {}", chatId, ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
