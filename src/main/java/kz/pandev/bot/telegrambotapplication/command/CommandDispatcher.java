package kz.pandev.bot.telegrambotapplication.command;

import jakarta.annotation.PostConstruct;
import kz.pandev.bot.telegrambotapplication.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        }
    }

    public void dispatch(Update update, TelegramLongPollingBot bot) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();

        // Если текстовая команда
        if (message.hasText()) {
            String text = message.getText();
            BotCommand command = commandMap.get(text.split(" ")[0]);
            if (command != null) {
                command.execute(update, bot);
            } else {
                BotCommand unknownCommand = commandMap.get("unknown");
                if (unknownCommand != null) {
                    unknownCommand.execute(update, bot);
                }
            }
            return;
        }

        // Если просто прислали файл — обрабатываем Excel
        if (message.hasDocument()) {
            Document document = message.getDocument();
            String fileName = document.getFileName();
            String chatId = message.getChatId().toString();

            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                uploadService.processExcelFile(document.getFileId(), chatId, bot);
            } else {
                send(bot, chatId, "❌ Поддерживаются только файлы .xlsx (Excel 2007+).");
            }
        }
    }

    // Вспомогательный метод для отправки сообщений
    private void send(TelegramLongPollingBot bot, String chatId, String text) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
