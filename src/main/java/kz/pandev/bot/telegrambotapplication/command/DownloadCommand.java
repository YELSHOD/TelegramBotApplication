package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import kz.pandev.bot.telegrambotapplication.model.Category;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class DownloadCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(DownloadCommand.class);

    @Autowired
    private CategoryService categoryService;

    @Override
    public String getCommand() {
        return "/download";
    }

    @Override
    public void execute(Update update, TelegramLongPollingBot bot) {
        String chatId = update.getMessage().getChatId().toString();

        List<Category> categories = categoryService.getAllCategories();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Categories");

            // Заголовки
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Parent");
            headerRow.createCell(2).setCellValue("Note");

            int rowNum = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category.getName());

                String parentName = category.getParent() != null ? category.getParent().getName() : "Root";
                row.createCell(1).setCellValue(parentName);

                if ("Root".equals(parentName)) {
                    row.createCell(2).setCellValue("🔹 Категория верхнего уровня (без родителя)");
                }
            }

            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            InputFile inputFile = new InputFile();
            inputFile.setMedia(new ByteArrayInputStream(bytes), "categories.xlsx");

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(inputFile);

            log.info("Отправка Excel документа чату {}", chatId);
            bot.execute(sendDocument);

        } catch (IOException | TelegramApiException e) {
            log.error("Ошибка при генерации или отправке документа для чата {}: {}", chatId, e.getMessage());
            try {
                bot.execute(new SendMessage(chatId, "Произошла ошибка при генерации документа."));
            } catch (TelegramApiException ex) {
                log.error("Ошибка при отправке сообщения об ошибке для чата {}: {}", chatId, ex.getMessage());
            }
        }
    }
}
