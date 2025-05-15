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

/**
 * Команда для просмотра корневых категорий.
 * <p>
 * Получает все категории из сервиса, фильтрует корневые (без родителя),
 * формирует список имен и отправляет его пользователю.
 * В случае ошибки отправляет сообщение с описанием ошибки.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCategoriesCommand implements BotCommand {

    private final CategoryService categoryService;

    /**
     * Возвращает строковое имя команды.
     * @return имя команды "/viewcategories"
     */
    @Override
    public String getCommand() {
        return "/viewcategories";
    }

    /**
     * Обрабатывает входящее обновление Telegram.
     * Получает chatId из сообщения, запрашивает список категорий,
     * фильтрует корневые, отправляет список пользователю.
     * При ошибках логирует и информирует пользователя.
     *
     * @param update объект обновления от Telegram API
     * @param bot    бот для отправки сообщений
     */
    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        try {
            // Получаю список всех категорий и фильтрую только корневые (у которых нет родителя)
            String response = categoryService.getAllCategories().stream()
                    .filter(category -> category.getParent() == null)      // только root-категории
                    .map(Category::getName)                                         // оставляю только имя категории
                    .collect(Collectors.joining("\n"));                     // объединяю в строку через перенос строки

            // Если ничего не найдено, отправляю соответствующее сообщение
            if (response.isEmpty()) {
                response = "Корневые категории не найдены.";
            }

            // Отправляю пользователю список категорий
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());

            log.info("Команда /viewcategories успешно выполнена для чата {}", chatId);

        } catch (Exception e) {
            log.error("Ошибка при выполнении команды /viewcategories для чата {}: {}", chatId, e.getMessage());
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
