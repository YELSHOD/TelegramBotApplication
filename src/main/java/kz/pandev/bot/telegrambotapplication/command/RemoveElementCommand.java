package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveElementCommand implements BotCommand {

    private final CategoryService categoryService;
    private static final int CATEGORIES_PER_PAGE = 5;

    @Override
    public String getCommand() {
        return "/removeelement";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            if (callbackData.startsWith("DELETE:")) {
                String categoryName = callbackData.substring("DELETE:".length());
                try {
                    categoryService.deleteCategoryByName(categoryName);
                    sendCallbackNotification(bot, update, "✅ Удалено: " + categoryName);
                    showCategoryButtonsWithWarning(bot, chatId, 1); // обновляем список
                } catch (IllegalArgumentException e) {
                    sendCallbackNotification(bot, update, "❌ Ошибка удаления: " + e.getMessage());
                }
            } else if (callbackData.startsWith("PAGE:")) {
                int page = Integer.parseInt(callbackData.substring("PAGE:".length()));
                showCategoryButtonsWithWarning(bot, chatId, page);
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText().trim();

            if (messageText.equals("➖ Удалить элемент")) {
                showCategoryButtonsWithWarning(bot, chatId, 1);
            } else {
                String[] parts = messageText.split("\\s+");
                if (parts.length == 2) {
                    String categoryName = parts[1];
                    try {
                        categoryService.deleteCategoryByName(categoryName);
                        sendText(bot, chatId, "✅ Категория '" + categoryName + "' успешно удалена.");
                    } catch (IllegalArgumentException e) {
                        sendText(bot, chatId, "❌ Ошибка: " + e.getMessage());
                    }
                } else {
                    sendText(bot, chatId, "❗ Неправильный формат. Пример: /removeelement НазваниеКатегории");
                }
            }
        }
    }

    private void showCategoryButtonsWithWarning(TelegramLongPollingBot bot, String chatId, int page) {
        List<String> categoryNames = categoryService.getAllCategoryDtos().stream()
                .map(dto -> dto.getName())
                .collect(Collectors.toList());

        List<String> buttonTexts = categoryService.getAllCategoryDtos().stream()
                .map(dto -> (dto.isParent() ? "ROOT " : "") + dto.getName())
                .collect(Collectors.toList());

        int startIndex = (page - 1) * CATEGORIES_PER_PAGE;
        int endIndex = Math.min(startIndex + CATEGORIES_PER_PAGE, categoryNames.size());

        List<String> categoriesToShow = categoryNames.subList(startIndex, endIndex);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonTexts.get(i));
            button.setCallbackData("DELETE:" + categoryNames.get(i)); // строгое имя
            rows.add(List.of(button));
        }


        // Навигация
        if (startIndex > 0) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("◀️ Предыдущая");
            prevButton.setCallbackData("PAGE:" + (page - 1));
            rows.add(List.of(prevButton));
        }

        if (endIndex < categoryNames.size()) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("▶️ Следующая");
            nextButton.setCallbackData("PAGE:" + (page + 1));
            rows.add(List.of(nextButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        String text = """
                ⚠️ Внимание!
                Если вы удалите родительскую категорию (ROOT), все дочерние категории тоже будут удалены.

                👇 Выберите категорию для удаления:
                """;

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .build();

        try {
            bot.execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке клавиатуры: {}", e.getMessage());
        }
    }

    private void sendText(TelegramLongPollingBot bot, String chatId, String text) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
        }
    }

    private void sendCallbackNotification(TelegramLongPollingBot bot, Update update, String message) {
        try {
            bot.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .text(message)
                    .showAlert(false)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке callback-уведомления: {}", e.getMessage());
        }
    }
}
