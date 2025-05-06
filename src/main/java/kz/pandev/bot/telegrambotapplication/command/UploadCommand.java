package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCommand implements BotCommand {

    private final UploadService uploadService;

    @Override
    public String getCommand() {
        return "/upload";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();

        if (message.hasDocument()) {
            Document document = message.getDocument();
            String fileName = document.getFileName();
            String mimeType = document.getMimeType();

            log.info("Получен файл: name={}, mime={}", fileName, mimeType);

            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                try {
                    uploadService.processExcelFile(document.getFileId(), chatId, bot);
                } catch (Exception e) {
                    log.error("Ошибка при обработке Excel-файла", e);
                    send(bot, chatId, "❌ Произошла ошибка при обработке файла. Убедитесь, что он соответствует шаблону.");
                }
            } else {
                send(bot, chatId, "❌ Поддерживаются только файлы формата .xlsx (Excel 2007+).");
            }

        } else {
            // Если файл не прикреплён — шлём шаблон и инструкцию
            try {
                uploadService.sendExcelTemplate(chatId, bot);
                send(bot, chatId, """
                        📤 Отправьте сюда Excel-файл с категориями.

                        Формат таблицы:
                        Parent Category | Child Category

                        Пример:
                        Еда     | Пицца
                        Еда     | Бургер
                        Телефон | Айфон
                        """);
            } catch (Exception e) {
                log.error("Ошибка при отправке шаблона Excel", e);
                send(bot, chatId, "❌ Ошибка при отправке шаблона Excel.");
            }
        }
    }

    private void send(TelegramLongPollingBot bot, String chatId, String text) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в Telegram", e);
        }
    }
}
