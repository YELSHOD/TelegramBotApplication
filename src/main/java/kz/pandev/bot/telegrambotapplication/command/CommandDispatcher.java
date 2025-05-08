package kz.pandev.bot.telegrambotapplication.command;

import jakarta.annotation.PostConstruct;
import kz.pandev.bot.telegrambotapplication.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandDispatcher {

    private final Map<String, BotCommand> commandMap = new HashMap<>();
    private final List<BotCommand> commands;
    private final UploadService uploadService;

    @PostConstruct
    private void init() {
        for (BotCommand command : commands) {
            commandMap.put(command.getCommand(), command);
            log.info("Команда '{}' зарегистрирована", command.getCommand());
        }
        log.info("Инициализация CommandDispatcher завершена. Зарегистрировано {} команд.", commandMap.size());
    }

    public void dispatch(Update update, TelegramLongPollingBot bot) {
        if (update.hasMessage()) {
            log.debug("Получено сообщение от пользователя: {}", update.getMessage().getText());
            handleMessage(update, bot);
        } else if (update.hasCallbackQuery()) {
            log.debug("Получен callback от пользователя: {}", update.getCallbackQuery().getData());
            handleCallback(update, bot);
        } else {
            log.warn("Получен неизвестный тип обновления: {}", update);
        }
    }

    private void handleMessage(Update update, TelegramLongPollingBot bot) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText().trim();
            log.info("Обработка текстового сообщения: {}", text);

            String mappedCommand = switch (text) {
                case "📘 Справка" -> "/help";
                case "➕ Добавить элемент" -> "/addelement";
                case "➖ Удалить элемент" -> "/removeelement";
                case "🌳 Дерево категорий" -> "/viewtree";
                case "📥 Скачать Excel" -> "/download";
                case "📊 Импорт Excel" -> "/upload";
                case "👁 Просмотр категорий" -> "/viewcategories";
                default -> text.split(" ")[0];
            };

            BotCommand command = commandMap.get(mappedCommand);
            if (command != null) {
                log.info("Выполнение команды: {}", mappedCommand);
                command.execute(update, bot);
            } else {
                log.warn("Неизвестная команда: {}", mappedCommand);
                handleUnknownCommand(update, bot, message.getChatId().toString());
            }

        } else if (message.hasDocument()) {
            Document document = message.getDocument();
            String fileName = document.getFileName();
            String chatId = message.getChatId().toString();

            log.info("Получен файл: {}", fileName);

            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                uploadService.processExcelFile(document.getFileId(), chatId, bot);
            } else {
                log.warn("Получен неподдерживаемый файл: {}", fileName);
                send(bot, chatId, "❌ Поддерживаются только файлы .xlsx (Excel 2007+).");
            }
        }
    }

    private void handleCallback(Update update, TelegramLongPollingBot bot) {
        CallbackQuery callback = update.getCallbackQuery();
        String data = callback.getData();
        log.info("Обработка callback-запроса: {}", data);

        BotCommand command = null;

        if (data.startsWith("DELETE:") || data.startsWith("PAGE:")) {
            command = commandMap.get("/removeelement");
        } else {
            command = commandMap.get(data);
        }

        if (command != null) {
            log.info("Выполнение inline-команды: {}", data);
            command.execute(update, bot);
        } else {
            log.warn("Неизвестная inline-команда: {}", data);
            handleUnknownCommand(update, bot, callback.getMessage().getChatId().toString());
        }
    }

    private void handleUnknownCommand(Update update, TelegramLongPollingBot bot, String chatId) {
        BotCommand unknownCommand = commandMap.get("unknown");
        if (unknownCommand != null) {
            unknownCommand.execute(update, bot);
        } else {
            send(bot, chatId, "❗ Неизвестная команда. Пожалуйста, используйте /help для получения помощи.");
        }
    }

    private void send(TelegramLongPollingBot bot, String chatId, String text) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения пользователю {}: {}", chatId, e.getMessage(), e);
        }
    }
}
