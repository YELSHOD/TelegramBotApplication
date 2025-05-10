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

        List<Category> allCategories = categoryService.getAllCategories();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Категории");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Название");

            int[] rowNum = {1}; // Используем массив для передачи по ссылке в рекурсию

            // Старт с корневых категорий
            allCategories.stream()
                    .filter(c -> c.getParent() == null)
                    .forEach(c -> writeCategoryRow(sheet, c, 0, rowNum, allCategories));

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
            log.error("Ошибка при генерации или отправке Excel: {}", e.getMessage());
            try {
                bot.execute(new SendMessage(chatId, "⚠️ Ошибка при генерации Excel-файла."));
            } catch (TelegramApiException ex) {
                log.error("Ошибка при отправке сообщения об ошибке: {}", ex.getMessage());
            }
        }
    }

    private void writeCategoryRow(Sheet sheet, Category category, int level, int[] rowNum, List<Category> allCategories) {
        Row row = sheet.createRow(rowNum[0]++);
        String indent = "  ".repeat(level) + (level > 0 ? "└─ " : "");
        row.createCell(0).setCellValue(indent + category.getName());

        // Применяем стиль для родительских категорий
        if (level == 0) {
            CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
            Font boldFont = sheet.getWorkbook().createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            row.getCell(0).setCellStyle(boldStyle);
        }

        // Рекурсивно обрабатываем подкатегории
        allCategories.stream()
                .filter(c -> category.equals(c.getParent()))
                .forEach(child -> writeCategoryRow(sheet, child, level + 1, rowNum, allCategories));
    }

}
