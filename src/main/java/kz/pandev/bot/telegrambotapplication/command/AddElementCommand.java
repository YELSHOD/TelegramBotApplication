package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

/**
 * Команда /addelement.
 * Отображает пользователю inline-кнопки для добавления новой категории или подкатегории.
 * <p>
 * Ожидает ввод в виде команды или клика по кнопке "➕ Добавить элемент".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddElementCommand implements BotCommand {

    private final CategoryService categoryService;

    /**
     * Возвращает строку команды, с которой связан данный обработчик.
     */
    @Override
    public String getCommand() {
        return "/addelement";
    }

    /**
     * Выполняет логику команды.
     * Если пользователь ввёл команду правильно — показываются inline-кнопки выбора типа элемента.
     * В противном случае отправляется сообщение об ошибке.
     *
     * @param update объект обновления от Telegram, содержащий сообщение от пользователя
     * @param bot экземпляр TelegramLongPollingBot, через который отправляются ответы пользователю
     */
    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();
        String fullText = update.getMessage().getText().trim();

        if (fullText.equals("/addelement") || fullText.equals("➕ Добавить элемент")) {
            showCategoryOptions(bot, chatId);
        } else {
            sendError(bot, chatId, """
                ❌ Неправильный формат команды.
                Нажмите на кнопку и следуйте инструкциям.
            """);
        }
    }


    /**
     * Отправляет пользователю inline-кнопки с вариантами: добавить категорию или подкатегорию.
     */
    private void showCategoryOptions(TelegramLongPollingBot bot, String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите, что хотите добавить:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = List.of(
                InlineKeyboardButton.builder()
                        .text("📁 Категорию")
                        .callbackData("ADD_CATEGORY")  // <-- синхронизировано с CommandDispatcher
                        .build(),
                InlineKeyboardButton.builder()
                        .text("📂 Подкатегорию")
                        .callbackData("ADD_SUBCATEGORY")  // <-- синхронизировано с CommandDispatcher
                        .build()
        );
        markup.setKeyboard(List.of(row));
        message.setReplyMarkup(markup);

        try {
            bot.execute(message);
            log.info("Пользователю {} показаны кнопки выбора добавления категории/подкатегории", chatId);
        } catch (Exception e) {
            log.error("Ошибка при отправке inline-кнопок: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправляет сообщение об ошибке при некорректном формате команды.
     */
    private void sendError(TelegramLongPollingBot bot, String chatId, String message) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .build());
            log.warn("Пользователю {} отправлено сообщение об ошибке формата команды", chatId);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения об ошибке: {}", e.getMessage(), e);
        }
    }
}
