package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.model.Category;
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


@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveElementCommand implements BotCommand {

    private final CategoryService categoryService;
    private static final int ITEMS_PER_PAGE = 5;

    @Override
    public String getCommand() {
        return "/removeelement";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            if (data.startsWith("PAGE_CATEGORY:")) {
                int page = Integer.parseInt(data.substring("PAGE_CATEGORY:".length()));
                showCategorySelection(bot, chatId, page);
                return;
            }

            if (data.startsWith("SELECT_CATEGORY:")) {
                Long parentId = Long.parseLong(data.substring("SELECT_CATEGORY:".length()));
                Category parent = categoryService.getCategoryById(parentId);
                if (parent != null) {
                    showSubcategorySelection(bot, chatId, parent);
                } else {
                    sendCallback(bot, update, "❌ Родительская категория не найдена.");
                }
                return;
            }

            if (data.startsWith("DELETE_SUBCATEGORY:")) {
                String[] parts = data.split(":", 3);
                Long childId = Long.parseLong(parts[1]);
                Long parentId = Long.parseLong(parts[2]);

                try {
                    Category child = categoryService.getCategoryById(childId);
                    categoryService.deleteCategoryById(childId);
                    sendCallback(bot, update, "✅ Подкатегория \"" + child.getName() + "\" удалена.");
                } catch (Exception e) {
                    sendCallback(bot, update, "❌ Ошибка: " + e.getMessage());
                }
                Category parent = categoryService.getCategoryById(parentId);
                if (parent != null) {
                    showSubcategorySelection(bot, chatId, parent);
                }
                return;
            }

            if (data.startsWith("DELETE_CATEGORY:")) {
                Long parentId = Long.parseLong(data.substring("DELETE_CATEGORY:".length()));
                try {
                    Category parent = categoryService.getCategoryById(parentId);
                    categoryService.deleteCategoryById(parentId);
                    sendCallback(bot, update, "✅ Категория \"" + parent.getName() + "\" и все её подкатегории удалены.");
                } catch (Exception e) {
                    sendCallback(bot, update, "❌ Ошибка: " + e.getMessage());
                }
                showCategorySelection(bot, chatId, 1);
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();
            String chatId = update.getMessage().getChatId().toString();
            if ("➖ Удалить элемент".equals(text)) {
                showCategorySelection(bot, chatId, 1);
            }
        }
    }

    private void showCategorySelection(TelegramLongPollingBot bot, String chatId, int page) {
        List<Category> roots = categoryService.getAllCategories().stream()
                .filter(c -> c.getParent() == null)
                .toList();

        int total = roots.size();
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, total);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Category cat = roots.get(i);
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(cat.getName())
                            .callbackData("SELECT_CATEGORY:" + cat.getId()) // <-- Исправлено: используй ID, а не название
                            .build()
            ));
        }

        if (start > 0) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text("◀️ Назад")
                            .callbackData("PAGE_CATEGORY:" + (page - 1))
                            .build()
            ));
        }
        if (end < total) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text("▶️ Вперёд")
                            .callbackData("PAGE_CATEGORY:" + (page + 1))
                            .build()
            ));
        }

        InlineKeyboardMarkup mk = new InlineKeyboardMarkup(rows);
        send(bot, chatId,
                String.format("👇 Выберите категорию (страница %d/%d):", page, (int) Math.ceil((double) total / ITEMS_PER_PAGE)),
                mk
        );
    }

    private void showSubcategorySelection(TelegramLongPollingBot bot, String chatId, Category parent) {
        List<Category> subs = categoryService.getAllCategories().stream()
                .filter(c -> c.getParent() != null && c.getParent().getId().equals(parent.getId()))
                .toList();

        StringBuilder header = new StringBuilder();
        header.append("👇 Выберите подкатегорию для удаления из \"").append(parent.getName()).append("\":\n\n")
                .append("⚠️ Если вы удалите категорию, то все её подкатегории тоже будут удалены.\n\n");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category c : subs) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(c.getName())
                            .callbackData("DELETE_SUBCATEGORY:" + c.getId() + ":" + parent.getId()) // <-- Используем ID подкатегории и родителя
                            .build()
            ));
        }

        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🗑️ Удалить категорию")
                        .callbackData("DELETE_CATEGORY:" + parent.getId()) // <-- Используем ID родительской категории
                        .build()
        ));

        InlineKeyboardMarkup mk = new InlineKeyboardMarkup(rows);
        send(bot, chatId, header.toString(), mk);
    }

    private void send(TelegramLongPollingBot bot, String chatId, String text, InlineKeyboardMarkup mk) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(mk)
                    .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }

    private void sendCallback(TelegramLongPollingBot bot, Update update, String text) {
        try {
            bot.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .text(text)
                    .showAlert(false)
                    .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при обработке callback: {}", e.getMessage(), e);
        }
    }
}
