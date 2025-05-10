package kz.pandev.bot.telegrambotapplication.command;

import jakarta.annotation.PostConstruct;
import kz.pandev.bot.telegrambotapplication.enums.UserState;
import kz.pandev.bot.telegrambotapplication.model.Category;
import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import kz.pandev.bot.telegrambotapplication.service.TempInputCache;
import kz.pandev.bot.telegrambotapplication.service.UploadService;
import kz.pandev.bot.telegrambotapplication.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandDispatcher {

    private static final int PARENTS_PER_PAGE = 5;

    private final Map<String, BotCommand> commandMap = new HashMap<>();
    private final List<BotCommand> commands;
    private final UploadService uploadService;
    private final UserStateService userStateService;
    private final CategoryService categoryService;
    private final TempInputCache tempInputCache;

    @PostConstruct
    private void init() {
        for (BotCommand cmd : commands) {
            commandMap.put(cmd.getCommand(), cmd);
            log.info("Команда '{}' зарегистрирована", cmd.getCommand());
        }
        log.info("Инициализация CommandDispatcher завершена. Всего команд: {}", commandMap.size());
    }

    public void dispatch(Update update, TelegramLongPollingBot bot) {
        if (update.hasMessage()) {
            handleMessage(update, bot);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update, bot);
        } else {
            log.warn("Unsupported update: {}", update);
        }
    }

    private void handleMessage(Update update, TelegramLongPollingBot bot) {
        Message msg = update.getMessage();
        String chatId = msg.getChatId().toString();

        if (msg.hasText()) {
            String text = msg.getText().trim();
            log.info("Text: {}", text);

            UserState state = userStateService.getUserState(chatId);
            if (state == UserState.AWAITING_ROOT_CATEGORY_NAME) {
                processRootCategory(text, chatId, bot);
                return;
            }
            if (state == UserState.AWAITING_CHILD_CATEGORY_NAME) {
                processChildCategory(text, chatId, bot);
                return;
            }

            String mapped = switch (text) {
                case "📘 Справка"            -> "/help";
                case "➕ Добавить элемент"   -> "/addelement";
                case "➖ Удалить элемент"    -> "/removeelement";
                case "🌳 Дерево категорий"   -> "/viewtree";
                case "📥 Скачать Excel"      -> "/download";
                case "📊 Импорт Excel"       -> "/upload";
                case "👁 Просмотр категорий" -> "/viewcategories";
                default                       -> text.split(" ")[0];
            };

            BotCommand cmd = commandMap.get(mapped);
            if (cmd != null) {
                log.info("Exec command: {}", mapped);
                cmd.execute(update, bot);
            } else {
                log.warn("Unknown command: {}", mapped);
                commandMap.get("unknown").execute(update, bot);
            }
        } else if (msg.hasDocument()) {
            String fn = msg.getDocument().getFileName();
            log.info("File: {}", fn);
            if (fn != null && fn.toLowerCase().endsWith(".xlsx")) {
                uploadService.processExcelFile(msg.getDocument().getFileId(), chatId, bot);
            } else {
                send(bot, chatId, "❌ Поддерживаются только .xlsx файлы.");
            }
        }
    }

    private void handleCallback(Update update, TelegramLongPollingBot bot) {
        CallbackQuery cb = update.getCallbackQuery();
        String data = cb.getData();
        String chatId = cb.getMessage().getChatId().toString();
        log.info("Callback: {}", data);

        // --- DELETE ELEMENT FLOW ---
        if (data.startsWith("PAGE_CATEGORY:") ||
                data.startsWith("SELECT_CATEGORY:") ||
                data.startsWith("DELETE_SUBCATEGORY:") ||
                data.startsWith("DELETE_CATEGORY:")) {

            // Убираем проверку AWAITING_CATEGORY_DELETION, если он не используется
            BotCommand removeCmd = commandMap.get("/removeelement");
            if (removeCmd != null) {
                removeCmd.execute(update, bot);
            }
            return;
        }

        // --- ADD CATEGORY FLOW ---
        if ("ADD_CATEGORY".equals(data)) {
            userStateService.setUserStates(chatId, UserState.AWAITING_ROOT_CATEGORY_NAME);
            send(bot, chatId, "📝 Введите название новой корневой категории:");
            return;
        }

        if ("ADD_SUBCATEGORY".equals(data)) {
            showParentSelection(bot, chatId, 1);
            return;
        }

        if (data.startsWith("PAGE_PARENT:")) {
            int page = Integer.parseInt(data.substring("PAGE_PARENT:".length()));
            showParentSelection(bot, chatId, page);
            return;
        }

        if (data.startsWith("SELECT_PARENT:")) {
            String parent = data.substring("SELECT_PARENT:".length());
            tempInputCache.save(Long.valueOf(chatId), parent);
            userStateService.setUserStates(chatId, UserState.AWAITING_CHILD_CATEGORY_NAME);
            send(bot, chatId, "📝 Введите название подкатегории для \"" + parent + "\":");
            return;
        }

        // --- OTHER INLINE COMMANDS ---
        BotCommand cmd = commandMap.get(data);
        if (cmd != null) {
            cmd.execute(update, bot);
        } else {
            log.warn("Unknown inline-command: {} — ignored", data);
        }
    }

    private void showParentSelection(TelegramLongPollingBot bot, String chatId, int page) {
        List<Category> roots = categoryService.getAllCategories().stream()
                .filter(c -> c.getParent() == null)
                .toList();
        int total = roots.size();
        int start = (page - 1) * PARENTS_PER_PAGE;
        int end = Math.min(start + PARENTS_PER_PAGE, total);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            String name = roots.get(i).getName();
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(name)
                            .callbackData("SELECT_PARENT:" + name)
                            .build()
            ));
        }
        if (start > 0) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text("◀️ Назад")
                            .callbackData("PAGE_PARENT:" + (page - 1))
                            .build()
            ));
        }
        if (end < total) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text("▶️ Вперёд")
                            .callbackData("PAGE_PARENT:" + (page + 1))
                            .build()
            ));
        }

        InlineKeyboardMarkup mk = new InlineKeyboardMarkup(rows);
        send(bot, chatId,
                String.format("🔽 Выберите родительскую категорию (%d–%d из %d):", start + 1, end, total),
                mk
        );
    }

    private void send(TelegramLongPollingBot bot, String chatId, String text) {
        send(bot, chatId, text, null);
    }

    private void send(TelegramLongPollingBot bot, String chatId, String text, InlineKeyboardMarkup mk) {
        try {
            SendMessage m = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(mk)
                    .build();
            bot.execute(m);
        } catch (Exception e) {
            log.error("Failed to send: {}", e.getMessage(), e);
        }
    }

    private void processRootCategory(String text, String chatId, TelegramLongPollingBot bot) {
        Category category = new Category();
        category.setName(text);
        category.setParent(null);

        categoryService.saveCategory(category);
        userStateService.clearStates(chatId);

        send(bot, chatId, "✅ Корневая категория успешно добавлена: \"" + text + "\"");
    }

    private void processChildCategory(String text, String chatId, TelegramLongPollingBot bot) {
        Long chatIdLong = Long.valueOf(chatId);
        String parentName = tempInputCache.get(chatIdLong);

        if (parentName == null) {
            send(bot, chatId, "❌ Не удалось найти выбранную родительскую категорию. Попробуйте заново.");
            userStateService.clearStates(chatId);
            return;
        }

        Category parent = categoryService.findByName(parentName).orElse(null);
        if (parent == null) {
            send(bot, chatId, "❌ Родительская категория \"" + parentName + "\" не найдена.");
            userStateService.clearStates(chatId);
            return;
        }

        Category subcategory = new Category();
        subcategory.setName(text);
        subcategory.setParent(parent);

        categoryService.saveCategory(subcategory);
        userStateService.clearStates(chatId);
        tempInputCache.clear(chatIdLong);

        send(bot, chatId, "✅ Подкатегория \"" + text + "\" успешно добавлена к категории \"" + parentName + "\"");
        userStateService.clearStates(chatId);

    }
}
