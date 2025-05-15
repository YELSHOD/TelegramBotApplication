package kz.pandev.bot.telegrambotapplication.command;

import kz.pandev.bot.telegrambotapplication.service.CategoryService;
import kz.pandev.bot.telegrambotapplication.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

/**
 * Команда для генерации и отправки Excel-файла со списком категорий.
 * Формирует иерархический список категорий с отступами и выделением родительских категорий.
 */

@Slf4j
@Component
public class DownloadCommand implements BotCommand {


    private final CategoryService categoryService;

    public DownloadCommand(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Возвращает строку команды, с которой связан данный обработчик.
     */
    @Override
    public String getCommand() {
        return "/download";
    }

    /**
     * Выполняет логику команды: генерирует Excel-файл с категориями и отправляет его пользователю.
     * В случае ошибки отправляет сообщение с информацией об ошибке.
     *
     * @param update объект с обновлением от Telegram
     * @param bot    экземпляр бота для отправки сообщений и документов
     */
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

    /**
     * Записывает категорию и её подкатегории в Excel лист с отступами для иерархии.
     * Родительские категории выделяются жирным шрифтом.
     *
     * @param sheet         лист Excel, куда записываются данные
     * @param category      категория для записи
     * @param level         уровень вложенности категории (0 — корневая)
     * @param rowNum        текущий номер строки (передаётся как массив для изменения внутри рекурсии)
     * @param allCategories список всех категорий для поиска подкатегорий
     */
    private void writeCategoryRow(Sheet sheet, Category category, int level, int[] rowNum, List<Category> allCategories) {
        Row row = sheet.createRow(rowNum[0]++);
        String indent = "  ".repeat(level) + (level > 0 ? "└─ " : "");
        row.createCell(0).setCellValue(indent + category.getName());

        if (level == 0) {
            CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
            Font boldFont = sheet.getWorkbook().createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            row.getCell(0).setCellStyle(boldStyle);
        }

        allCategories.stream()
                .filter(c -> category.equals(c.getParent()))
                .forEach(child -> writeCategoryRow(sheet, child, level + 1, rowNum, allCategories));
    }

}
