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
        String fullText = update.getMessage().getText().trim();
        String chatId = update.getMessage().getChatId().toString();
        String[] parts = fullText.split("\\s+");

        // Убедимся, что первый элемент — это /addElement
        if (!parts[0].equals("/addElement")) {
            // Примитивная защита — если пришло нечто типа "➕ Добавить элемент"
            if (fullText.equals("➕ Добавить элемент")) {
                // Просто показываем инструкцию
                sendInstruction(bot, chatId);
                return;
            } else {
                sendError(bot, chatId, "❌ Неверный вызов команды.");
                return;
            }
        }

        try {
            String response;

            if (parts.length == 1) {
                sendInstruction(bot, chatId);
                log.info("Пользователь {} запросил инструкцию по добавлению категории.", chatId);
                return;

            } else if (parts.length == 2) {
                String rootCategoryName = parts[1];
                categoryService.createRootCategory(rootCategoryName);
                response = "✅ Корневая категория '" + rootCategoryName + "' добавлена.";
                log.debug("Корневая категория '{}' добавлена пользователем {}.", rootCategoryName, chatId);

            } else if (parts.length == 3) {
                String parentName = parts[1];
                String childName = parts[2];

                if (categoryService.existsByName(parentName)) {
                    categoryService.createChildCategory(parentName, childName);
                    response = "✅ Дочерняя категория '" + childName + "' для родительской '" + parentName + "' добавлена.";
                    log.debug("Дочерняя категория '{}' добавлена к '{}' пользователем {}.", childName, parentName, chatId);
                } else {
                    response = "⚠️ Родительская категория '" + parentName + "' не найдена.";
                    log.warn("Не найдена родительская категория '{}' от пользователя {}.", parentName, chatId);
                }

            } else {
                response = """
                ❌ Неправильный формат команды.
                
                Используйте:
                /addElement Категория
                /addElement Родитель Дочерняя
                """;
                log.warn("Неверный формат команды /addElement от пользователя {}", chatId);
            }

            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());

        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /addElement для чата {}: {}", chatId, e.getMessage());
            sendError(bot, chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    private void sendInstruction(TelegramLongPollingBot bot, String chatId) {
        String response = """
        ℹ️ Чтобы добавить категорию, используйте один из форматов:
        
        ➕ /addElement {ИмяКатегории} – создать корневую категорию
        ➕ /addElement {Родитель} {ИмяКатегории} – создать дочернюю категорию
        
        📌 Примеры:
        /addElement Продукты
        /addElement Продукты Овощи
        """;

        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке инструкции: {}", e.getMessage());
        }
    }

    private void sendError(TelegramLongPollingBot bot, String chatId, String message) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения об ошибке для чата {}: {}", chatId, e.getMessage());
        }
    }

}
